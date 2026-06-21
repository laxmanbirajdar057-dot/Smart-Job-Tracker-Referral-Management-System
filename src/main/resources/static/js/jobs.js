// Dashboard page: loads jobs + stats, and handles the add/edit/delete modal.
// Now referral-aware: every job can carry referral-request details
// (referrer, contact, relation, referral status, notes) independent of
// the application status.

const STATUS_VALUES = [
  "SAVED",
  "APPLIED",
  "SCREENING",
  "TECHNICAL_ROUND",
  "HR_ROUND",
  "OFFER",
  "REJECTED",
  "NEGOTIATING",
];

const JOB_TYPE_VALUES = ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "TEMPORARY"];

const REFERRAL_STATUS_VALUES = ["NOT_REQUESTED", "REQUESTED", "REFERRED", "NO_RESPONSE", "DECLINED"];

let allJobs = [];
let editingJobId = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!Auth.isLoggedIn()) {
    window.location.href = "/api/login-page";
    return;
  }

  const user = Auth.getUser();
  if (user) {
    const nameEl = document.getElementById("userName");
    if (nameEl) {
      nameEl.textContent = [user.firstName, user.lastName].filter(Boolean).join(" ") || user.email;
    }
  }

  document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());
  document.getElementById("addJobBtn").addEventListener("click", () => openModal());
  document.getElementById("statusFilter").addEventListener("change", applyFilter);
  document.getElementById("referralFilter").addEventListener("change", applyFilter);
  document.getElementById("jobForm").addEventListener("submit", handleSaveJob);
  document.getElementById("cancelBtn").addEventListener("click", closeModal);
  document.getElementById("hasReferral").addEventListener("change", toggleReferralFields);
  document.getElementById("modalOverlay").addEventListener("click", (e) => {
    if (e.target.id === "modalOverlay") closeModal();
  });

  loadDashboard();
});

async function loadDashboard() {
  await Promise.all([loadStats(), loadJobs()]);
}

async function loadStats() {
  try {
    const count = await apiFetch("/jobs/stats/count");
    document.getElementById("totalCount").textContent = count;
  } catch (err) {
    console.error("Failed to load job count", err);
  }
}

async function loadJobs() {
  const container = document.getElementById("jobList");
  container.innerHTML = `<div class="empty-state">Loading your applications...</div>`;

  try {
    const response = await apiFetch("/jobs");
    allJobs = response.data || [];
    renderStatusBreakdown(allJobs);
    applyFilter();
  } catch (err) {
    container.innerHTML = `<div class="empty-state">Couldn't load jobs: ${escapeHtml(err.message)}</div>`;
  }
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
        <p>Click "Add Job" to start tracking one — and don't forget to log any referral you line up.</p>
      </div>`;
    return;
  }

  container.innerHTML = jobs
    .map((job) => {
      const statusLabel = formatLabel(job.status);
      const typeLabel = job.jobType ? formatLabel(job.jobType) : null;
      const referralStatus = job.referralStatus || "NOT_REQUESTED";
      const referralLabel = formatLabel(referralStatus);

      const referralChip = job.hasReferral
        ? `<span class="referral-chip referral-${referralStatus}" title="${job.referrerName ? escapeHtml(job.referrerName) : "Referral"}">
             🤝 ${referralLabel}${job.referrerName ? ` · ${escapeHtml(job.referrerName)}` : ""}
           </span>`
        : `<span class="referral-chip referral-NOT_REQUESTED">No referral yet</span>`;

      return `
        <div class="job-card" data-id="${job.id}">
          <div class="job-main">
            <h3>${escapeHtml(job.roleName)}</h3>
            <div class="company">${escapeHtml(job.company)}</div>
            <div class="job-meta">
              ${typeLabel ? `<span>${typeLabel}</span>` : ""}
              ${job.location ? `<span>${escapeHtml(job.location)}</span>` : ""}
              ${job.salary ? `<span>${escapeHtml(job.salary)}</span>` : ""}
              ${job.deadline ? `<span>Deadline: ${job.deadline}</span>` : ""}
            </div>
            <div class="referral-row">${referralChip}</div>
          </div>
          <div class="job-actions">
            <span class="status-badge status-${job.status}">${statusLabel}</span>
            <div class="job-actions-row">
              <button class="btn btn-secondary btn-sm" onclick="openModal(${job.id})">Edit</button>
              <button class="btn btn-danger btn-sm" onclick="handleDeleteJob(${job.id})">Delete</button>
            </div>
          </div>
        </div>`;
    })
    .join("");
}

function openModal(jobId = null) {
  editingJobId = jobId;
  const form = document.getElementById("jobForm");
  form.reset();
  populateSelectOptions();

  document.getElementById("modalTitle").textContent = jobId ? "Edit Job" : "Add Job";

  if (jobId) {
    const job = allJobs.find((j) => j.id === jobId);
    if (job) {
      document.getElementById("company").value = job.company || "";
      document.getElementById("roleName").value = job.roleName || "";
      document.getElementById("jobUrl").value = job.jobUrl || "";
      document.getElementById("location").value = job.location || "";
      document.getElementById("salary").value = job.salary || "";
      document.getElementById("companySize").value = job.companySize || "";
      document.getElementById("deadline").value = job.deadline || "";
      document.getElementById("jobType").value = job.jobType || "";
      document.getElementById("status").value = job.status || "SAVED";
      document.getElementById("jobDescription").value = job.jobDescription || "";
      document.getElementById("notes").value = job.notes || "";

      document.getElementById("hasReferral").checked = !!job.hasReferral;
      document.getElementById("referrerName").value = job.referrerName || "";
      document.getElementById("referrerRelation").value = job.referrerRelation || "";
      document.getElementById("referrerContact").value = job.referrerContact || "";
      document.getElementById("referralRequestedDate").value = job.referralRequestedDate || "";
      document.getElementById("referralStatus").value = job.referralStatus || "NOT_REQUESTED";
      document.getElementById("referralNotes").value = job.referralNotes || "";
    }
  } else {
    document.getElementById("status").value = "SAVED";
    document.getElementById("referralStatus").value = "NOT_REQUESTED";
  }

  toggleReferralFields();
  document.getElementById("modalOverlay").classList.add("visible");
}

function closeModal() {
  document.getElementById("modalOverlay").classList.remove("visible");
  editingJobId = null;
}

function toggleReferralFields() {
  const checked = document.getElementById("hasReferral").checked;
  document.getElementById("referralFields").classList.toggle("visible", checked);
}

function populateSelectOptions() {
  const typeSelect = document.getElementById("jobType");
  const statusSelect = document.getElementById("status");
  const referralStatusSelect = document.getElementById("referralStatus");

  typeSelect.innerHTML =
    `<option value="">Select type</option>` +
    JOB_TYPE_VALUES.map((v) => `<option value="${v}">${formatLabel(v)}</option>`).join("");

  statusSelect.innerHTML = STATUS_VALUES.map(
    (v) => `<option value="${v}">${formatLabel(v)}</option>`
  ).join("");

  referralStatusSelect.innerHTML = REFERRAL_STATUS_VALUES.map(
    (v) => `<option value="${v}">${formatLabel(v)}</option>`
  ).join("");
}

async function handleSaveJob(event) {
  event.preventDefault();
  const saveBtn = document.getElementById("saveBtn");

  const hasReferral = document.getElementById("hasReferral").checked;

  const payload = {
    company: document.getElementById("company").value.trim(),
    roleName: document.getElementById("roleName").value.trim(),
    jobUrl: document.getElementById("jobUrl").value.trim() || null,
    location: document.getElementById("location").value.trim() || null,
    salary: document.getElementById("salary").value.trim() || null,
    companySize: document.getElementById("companySize").value.trim() || null,
    deadline: document.getElementById("deadline").value || null,
    jobType: document.getElementById("jobType").value || null,
    status: document.getElementById("status").value || "SAVED",
    jobDescription: document.getElementById("jobDescription").value.trim() || null,
    notes: document.getElementById("notes").value.trim() || null,

    hasReferral: hasReferral,
    referrerName: hasReferral ? document.getElementById("referrerName").value.trim() || null : null,
    referrerRelation: hasReferral ? document.getElementById("referrerRelation").value.trim() || null : null,
    referrerContact: hasReferral ? document.getElementById("referrerContact").value.trim() || null : null,
    referralRequestedDate: hasReferral ? document.getElementById("referralRequestedDate").value || null : null,
    referralStatus: hasReferral ? document.getElementById("referralStatus").value || "REQUESTED" : "NOT_REQUESTED",
    referralNotes: hasReferral ? document.getElementById("referralNotes").value.trim() || null : null,
  };

  if (!payload.company || !payload.roleName) {
    showToast("Company and role are required.", "error");
    return;
  }

  setButtonLoading(saveBtn, true, "Saving...");

  try {
    if (editingJobId) {
      await apiFetch(`/jobs/${editingJobId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      showToast("Job updated.");
    } else {
      // postedDate is only on create, default to today
      payload.postedDate = new Date().toISOString().slice(0, 10);
      await apiFetch("/jobs", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      showToast("Job added.");
    }

    closeModal();
    loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to save job.", "error");
  } finally {
    setButtonLoading(saveBtn, false);
  }
}

async function handleDeleteJob(jobId) {
  if (!confirm("Delete this application? This can't be undone.")) return;

  try {
    await apiFetch(`/jobs/${jobId}`, { method: "DELETE" });
    showToast("Job deleted.");
    loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to delete job.", "error");
  }
}

function formatLabel(value) {
  if (!value) return "";
  return value
    .toLowerCase()
    .split("_")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
