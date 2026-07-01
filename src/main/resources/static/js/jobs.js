// jobs.js — ReferTrack dashboard logic
// All form field IDs use job_ prefix to avoid collision with referral modal

const STATUS_VALUES = [
  "SAVED", "APPLIED", "SCREENING", "TECHNICAL_ROUND",
  "HR_ROUND", "OFFER", "REJECTED", "NEGOTIATING",
];
const JOB_TYPE_VALUES = ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "TEMPORARY"];
const REFERRAL_STATUS_VALUES = ["NOT_REQUESTED", "REQUESTED", "REFERRED", "NO_RESPONSE", "DECLINED"];

let allJobs = [];
let editingJobId = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!Auth.isLoggedIn()) {
    window.location.href = "/login-page";
    return;
  }

  // Set user name in topbar
  const user = Auth.getUser();
  if (user) {
    const nameEl = document.getElementById("userName");
    if (nameEl) {
      nameEl.textContent = [user.firstName, user.lastName].filter(Boolean).join(" ") || user.email;
    }
    const avatarEl = document.getElementById("userAvatar");
    if (avatarEl && user.firstName) {
      avatarEl.textContent = (user.firstName[0] + (user.lastName ? user.lastName[0] : "")).toUpperCase();
    }
  }

  // Wire up all listeners
  document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());
  document.getElementById("addJobBtn").addEventListener("click", () => openModal());
  document.getElementById("statusFilter").addEventListener("change", applyFilter);
  document.getElementById("referralFilter").addEventListener("change", applyFilter);
  document.getElementById("jobForm").addEventListener("submit", handleSaveJob);
  document.getElementById("cancelBtn").addEventListener("click", closeModal);
  document.getElementById("job_hasReferral").addEventListener("change", toggleReferralFields);
  document.getElementById("modalOverlay").addEventListener("click", (e) => {
    if (e.target.id === "modalOverlay") closeModal();
  });
  document.getElementById("job_resumeFile").addEventListener("change", (e) => {
    const file = e.target.files[0];
    document.getElementById("job_resumeFileName").textContent = file ? file.name : "No file chosen";
  });

  loadDashboard();
});

// ===================== LOAD =====================

async function loadDashboard() {
  await Promise.all([loadStats(), loadJobs()]);
}

async function loadStats() {
  try {
    const count = await apiFetch("/api/jobs/stats/count");
    document.getElementById("totalCount").textContent = count;
  } catch (err) {
    console.error("Failed to load job count", err);
  }
}

async function loadJobs() {
  const container = document.getElementById("jobList");
  container.innerHTML = `<div class="empty-state">Loading your applications...</div>`;
  try {
    const response = await apiFetch("/api/jobs");
    allJobs = response.data || [];
    renderStatusBreakdown(allJobs);
    applyFilter();
  } catch (err) {
    container.innerHTML = `<div class="empty-state">Couldn't load jobs: ${escapeHtml(err.message)}</div>`;
  }
}


// ===================== VIEW JOB =====================



function viewResume(resumeId) {
  const token = Auth.getToken();
  if (!token) {
    showToast("You are not logged in.", "error");
    return;
  }
  const url = `/api/resumes/${resumeId}/view?token=${encodeURIComponent(token)}`;
  window.open(url, "_blank");
}

function renderStatusBreakdown(jobs) {
  const applied = jobs.filter((j) => j.status !== "SAVED").length;
  const offers = jobs.filter((j) => j.status === "OFFER" || j.status === "NEGOTIATING").length;
  const rejected = jobs.filter((j) => j.status === "REJECTED").length;
  const referrals = jobs.filter((j) => j.referralStatus === "REFERRED").length;

  document.getElementById("appliedCount").textContent = applied;
  document.getElementById("offerCount").textContent = offers;
  document.getElementById("rejectedCount").textContent = rejected;
  document.getElementById("referralCount").textContent = referrals;
}

// ===================== FILTER + RENDER =====================

function applyFilter() {
  const status = document.getElementById("statusFilter").value;
  const referral = document.getElementById("referralFilter").value;

  let filtered = status ? allJobs.filter((j) => j.status === status) : allJobs;

  if (referral === "WITH") {
    filtered = filtered.filter((j) => j.hasReferral);
  } else if (referral) {
    filtered = filtered.filter((j) => (j.referralStatus || "NOT_REQUESTED") === referral);
  }

  renderJobs(filtered);
}

function renderJobs(jobs) {
  const container = document.getElementById("jobList");

  if (jobs.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>No applications here yet</h3>
        <p>Click "Add Job" to start tracking one.</p>
      </div>`;
    return;
  }

  container.innerHTML = jobs.map((job) => {
    const statusLabel = formatLabel(job.status);
    const typeLabel = job.jobType ? formatLabel(job.jobType) : null;
    const referralStatus = job.referralStatus || "NOT_REQUESTED";
    const referralLabel = formatLabel(referralStatus);

    // Referral chip
    const referralChip = job.hasReferral
      ? `<span class="referral-chip ref-${referralStatus.toLowerCase().replace('_', '-')}">
           🤝 ${referralLabel}${job.referrerName ? ` · ${escapeHtml(job.referrerName)}` : ""}
         </span>`
      : `<span class="referral-chip ref-none">No referral yet</span>`;

    // CV badge
const cvDisplay = job.resumeLabel || job.resumeFileName;
const cvBadge = cvDisplay && job.resumeId
  ? `<span class="cv-badge" onclick="viewResume(${job.resumeId})" style="cursor:pointer;" title="View ${escapeHtml(job.resumeFileName || '')}">
       📄 ${escapeHtml(cvDisplay)}${job.resumeFileName ? ` <span class="cv-version">${escapeHtml(job.resumeFileName)}</span>` : ""}
     </span>`
  : `<span class="cv-badge cv-badge-empty" onclick="openModal(${job.id})">📎 Attach CV</span>`;

    // Company initial logo
    const initial = (job.company || "?")[0].toUpperCase();

    return `
      <div class="job-card" data-id="${job.id}">
        <div class="job-top">
          <div class="job-info">
            <div class="company-logo">${escapeHtml(initial)}</div>
            <div>
              <div class="job-title">${escapeHtml(job.roleName)}</div>
              <div class="job-company">${escapeHtml(job.company)}${job.location ? ` · ${escapeHtml(job.location)}` : ""}</div>
            </div>
          </div>
          <span class="status-badge status-${job.status}">${statusLabel}</span>
        </div>
        <div class="job-tags">
          ${typeLabel ? `<span class="job-tag">${typeLabel}</span>` : ""}
          ${job.salary ? `<span class="job-tag">${escapeHtml(job.salary)}</span>` : ""}
          ${job.deadline ? `<span class="job-tag">⏰ ${job.deadline}</span>` : ""}
        </div>
        <div class="job-footer">
          <div class="job-chips">
            ${referralChip}
            ${cvBadge}
          </div>
          <div class="job-actions-row">
            <button class="btn btn-secondary btn-sm" onclick="openModal(${job.id})">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="handleDeleteJob(${job.id})">Delete</button>
          </div>
        </div>
      </div>`;
  }).join("");
}

function renderAnalytics() {
  if (!allJobs || allJobs.length === 0) {
    document.getElementById("ana-breakdown").innerHTML =
      `<div style="color:var(--text-dim);font-size:13px;">No applications yet.</div>`;
    return;
  }

  const total    = allJobs.length;
  const applied  = allJobs.filter(j => j.status !== "SAVED").length;
  const offers   = allJobs.filter(j => j.status === "OFFER" || j.status === "NEGOTIATING").length;
  const rejected = allJobs.filter(j => j.status === "REJECTED").length;
  const referral = allJobs.filter(j => j.hasReferral).length;

  document.getElementById("ana-total").textContent    = total;
  document.getElementById("ana-applied").textContent  = applied;
  document.getElementById("ana-offer").textContent    = offers;
  document.getElementById("ana-rejected").textContent = rejected;
  document.getElementById("ana-referral").textContent = referral;

  const rate = total === 0 ? 0 : Math.round((referral / total) * 100);
  document.getElementById("ana-referral-rate").textContent = `${rate}%`;

  // Status breakdown bars
  const statusGroups = {};
  allJobs.forEach(j => {
    statusGroups[j.status] = (statusGroups[j.status] || 0) + 1;
  });

  document.getElementById("ana-breakdown").innerHTML = Object.entries(statusGroups)
    .sort((a, b) => b[1] - a[1])
    .map(([status, count]) => {
      const pct = Math.round((count / total) * 100);
      return `
        <div style="margin-bottom:10px;">
          <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:3px;">
            <span>${formatLabel(status)}</span>
            <span>${count} (${pct}%)</span>
          </div>
          <div style="background:var(--border);border-radius:4px;height:6px;">
            <div style="width:${pct}%;background:var(--accent);border-radius:4px;height:6px;transition:width 0.3s;"></div>
          </div>
        </div>`;
    }).join("");
}

// ===================== MODAL =====================

function openModal(jobId = null) {
  editingJobId = jobId;
  const form = document.getElementById("jobForm");
  form.reset();
  populateSelectOptions();
  resetResumeUI();

  document.getElementById("modalTitle").textContent = jobId ? "Edit Job" : "Add Job";

  if (jobId) {
    const job = allJobs.find((j) => j.id === jobId);
    if (job) {
      document.getElementById("job_company").value = job.company || "";
      document.getElementById("job_roleName").value = job.roleName || "";
      document.getElementById("job_jobUrl").value = job.jobUrl || "";
      document.getElementById("job_location").value = job.location || "";
      document.getElementById("job_salary").value = job.salary || "";
      document.getElementById("job_companySize").value = job.companySize || "";
      document.getElementById("job_deadline").value = job.deadline || "";
      document.getElementById("job_jobType").value = job.jobType || "";
      document.getElementById("job_status").value = job.status || "SAVED";
      document.getElementById("job_jobDescription").value = job.jobDescription || "";
      document.getElementById("job_notes").value = job.notes || "";

      // Resume
      document.getElementById("job_resumeLabel").value = job.resumeLabel || "";
      document.getElementById("job_resumeVersion").value = job.resumeVersion || "";
      if (job.resumeFileName) {
        document.getElementById("job_resumeFileName").textContent = "✅ " + job.resumeFileName;
      }

      // Referral
      document.getElementById("job_hasReferral").checked = !!job.hasReferral;
      document.getElementById("job_referrerName").value = job.referrerName || "";
      document.getElementById("job_referrerRelation").value = job.referrerRelation || "";
      document.getElementById("job_referrerContact").value = job.referrerContact || "";
      document.getElementById("job_referralRequestedDate").value = job.referralRequestedDate || "";
      document.getElementById("job_referralStatus").value = job.referralStatus || "NOT_REQUESTED";
      document.getElementById("job_referralNotes").value = job.referralNotes || "";

      populateJobReferralSelect(job.referralId || "");
    }
  } else {
    document.getElementById("job_status").value = "SAVED";
    document.getElementById("job_referralStatus").value = "NOT_REQUESTED";
    populateJobReferralSelect("");
  }

  toggleReferralFields();
  document.getElementById("modalOverlay").classList.add("visible");
}

function closeModal() {
  document.getElementById("modalOverlay").classList.remove("visible");
  editingJobId = null;
  resetResumeUI();
}

function resetResumeUI() {
  const fileInput = document.getElementById("job_resumeFile");
  if (fileInput) fileInput.value = "";
  const fileName = document.getElementById("job_resumeFileName");
  if (fileName) fileName.textContent = "No file chosen";
  const status = document.getElementById("job_resumeUploadStatus");
  if (status) status.textContent = "";
}

function toggleReferralFields() {
  const checked = document.getElementById("job_hasReferral").checked;
  document.getElementById("job_referralFields").classList.toggle("visible", checked);
}

function populateSelectOptions() {
  document.getElementById("job_jobType").innerHTML =
    `<option value="">Select type</option>` +
    JOB_TYPE_VALUES.map((v) => `<option value="${v}">${formatLabel(v)}</option>`).join("");

  document.getElementById("job_status").innerHTML =
    STATUS_VALUES.map((v) => `<option value="${v}">${formatLabel(v)}</option>`).join("");

  document.getElementById("job_referralStatus").innerHTML =
    REFERRAL_STATUS_VALUES.map((v) => `<option value="${v}">${formatLabel(v)}</option>`).join("");
}

// ===================== SAVE JOB =====================

async function handleSaveJob(event) {
  event.preventDefault();
  const saveBtn = document.getElementById("saveBtn");

  const hasReferral = document.getElementById("job_hasReferral").checked;

  const payload = {
    company: document.getElementById("job_company").value.trim(),
    roleName: document.getElementById("job_roleName").value.trim(),
    jobUrl: document.getElementById("job_jobUrl").value.trim() || null,
    location: document.getElementById("job_location").value.trim() || null,
    salary: document.getElementById("job_salary").value.trim() || null,
    companySize: document.getElementById("job_companySize").value.trim() || null,
    deadline: document.getElementById("job_deadline").value || null,
    jobType: document.getElementById("job_jobType").value || null,
    status: document.getElementById("job_status").value || "SAVED",
    jobDescription: document.getElementById("job_jobDescription").value.trim() || null,
    notes: document.getElementById("job_notes").value.trim() || null,
    referralId: document.getElementById("job_referralId").value || null,
    resumeLabel: document.getElementById("job_resumeLabel").value.trim() || null,
    resumeVersion: document.getElementById("job_resumeVersion").value.trim() || null,
    hasReferral,
    referrerName: hasReferral ? document.getElementById("job_referrerName").value.trim() || null : null,
    referrerRelation: hasReferral ? document.getElementById("job_referrerRelation").value.trim() || null : null,
    referrerContact: hasReferral ? document.getElementById("job_referrerContact").value.trim() || null : null,
    referralRequestedDate: hasReferral ? document.getElementById("job_referralRequestedDate").value || null : null,
    referralStatus: hasReferral ? document.getElementById("job_referralStatus").value || "REQUESTED" : "NOT_REQUESTED",
    referralNotes: hasReferral ? document.getElementById("job_referralNotes").value.trim() || null : null,
  };

  if (!payload.company || !payload.roleName) {
    showToast("Company and role are required.", "error");
    return;
  }

  setButtonLoading(saveBtn, true, "Saving...");

  try {
    let savedJob;
    if (editingJobId) {
      savedJob = await apiFetch(`/api/jobs/${editingJobId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      showToast("Job updated.");
    } else {
      payload.postedDate = new Date().toISOString().slice(0, 10);
      savedJob = await apiFetch("/api/jobs", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      showToast("Job saved.");
    }

    // Upload CV file if selected
    const fileInput = document.getElementById("job_resumeFile");
    if (fileInput.files.length > 0 && savedJob && savedJob.id) {
      await uploadResumeFile(savedJob.id, fileInput.files[0], payload.resumeLabel);
    }

    closeModal();
    loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to save job.", "error");
  } finally {
    setButtonLoading(saveBtn, false);
  }
}

// ===================== UPLOAD RESUME =====================


async function uploadResumeFile(jobId, file, label) {
  const statusEl = document.getElementById("job_resumeUploadStatus");
  statusEl.textContent = "Uploading CV...";
  try {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("label", label || file.name);
    const token = Auth.getToken();
    const res = await fetch("/api/resumes/upload", {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    });
    if (!res.ok) throw new Error("Upload failed");

    const uploadedResume = await res.json(); // ✅ get uploaded resume id

    // ✅ Link resume to job via PATCH
    await apiFetch(`/api/jobs/${jobId}`, {
      method: "PATCH",
      body: JSON.stringify({ resumeId: uploadedResume.id }),
    });

    statusEl.textContent = "✅ CV uploaded";
    statusEl.style.color = "#4cb782";
    if (typeof loadSidebarResumes === "function") loadSidebarResumes();
  } catch {
    statusEl.textContent = "⚠️ CV upload failed — job was saved";
    statusEl.style.color = "#e25c5c";
  }
}

// ===================== DELETE =====================

async function handleDeleteJob(jobId) {
  if (!confirm("Delete this application? This can't be undone.")) return;
  try {
    await apiFetch(`/api/jobs/${jobId}`, { method: "DELETE" });
    showToast("Job deleted.");
    loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to delete job.", "error");
  }
}

// ===================== HELPERS =====================

function formatLabel(value) {
  if (!value) return "";
  return value.toLowerCase().split("_").map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(" ");
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}