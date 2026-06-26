// referral.js — Referral network tab + sidebar widgets
// All referral modal IDs use ref_ prefix — no collision with job modal

const RELATIONSHIP_VALUES = [
  "COLLEGE_ALUMNI", "FORMER_COLLEAGUE", "CURRENT_COLLEAGUE",
  "FRIEND", "FAMILY", "LINKEDIN_CONNECTION", "RECRUITER", "OTHER",
];

let allReferrals = [];
let editingReferralId = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!Auth.isLoggedIn()) return;

  // Tab buttons in main content
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      switchTab(btn.dataset.tab);
      document.querySelectorAll(".tab-btn").forEach((b) =>
        b.classList.toggle("active", b.dataset.tab === btn.dataset.tab)
      );
    });
  });

  // Sidebar nav items
  document.querySelectorAll(".sidebar-item[data-tab]").forEach((item) => {
    item.addEventListener("click", () => {
      const tab = item.dataset.tab;
      switchTab(tab);
      document.querySelectorAll(".sidebar-item").forEach((s) => s.classList.remove("active"));
      item.classList.add("active");
      // Also sync tab buttons
      document.querySelectorAll(".tab-btn").forEach((b) =>
        b.classList.toggle("active", b.dataset.tab === tab)
      );
    });
  });

  // Referral modal triggers
  document.getElementById("addReferralBtn").addEventListener("click", () => openReferralModal());
  document.getElementById("quickAddReferralBtn").addEventListener("click", () => openReferralModal());
  document.getElementById("referralForm").addEventListener("submit", handleSaveReferral);
  document.getElementById("referralCancelBtn").addEventListener("click", closeReferralModal);
  document.getElementById("referralModalOverlay").addEventListener("click", (e) => {
    if (e.target.id === "referralModalOverlay") closeReferralModal();
  });

  // Quick resume upload from sidebar
  document.getElementById("quickResumeFile").addEventListener("change", handleQuickResumeUpload);

  loadReferrals();
  loadReferralStats();
  loadSidebarResumes();
});

// ===================== TAB SWITCH =====================

function switchTab(tab) {
  document.getElementById("tab-applications").classList.toggle("active", tab === "applications");
  document.getElementById("tab-network").classList.toggle("active", tab === "network");
}

// ===================== LOAD REFERRALS =====================

async function loadReferrals() {
  try {
    const response = await apiFetch("/referrals");
    allReferrals = response.data || [];
    renderReferralNetwork(allReferrals);
    renderReferralMiniList(allReferrals);
    populateJobReferralSelect();
  } catch (err) {
    const container = document.getElementById("referralList");
    if (container) {
      container.innerHTML = `<div class="empty-state">Couldn't load contacts: ${escapeHtml(err.message)}</div>`;
    }
    document.getElementById("referralMiniList").innerHTML =
      `<div style="font-size:12px;color:var(--text-dim);">No contacts yet.</div>`;
  }
}

async function loadReferralStats() {
  try {
    const stats = await apiFetch("/referrals/stats");
    document.getElementById("totalReferralsCount").textContent = stats.totalReferrals ?? 0;
    document.getElementById("totalViaReferralCount").textContent = stats.totalApplicationsViaReferral ?? 0;
    const rate = typeof stats.referralRatePercent === "number" ? stats.referralRatePercent : 0;
    document.getElementById("referralRatePercent").textContent = `${Math.round(rate)}%`;
  } catch (err) {
    console.error("Failed to load referral stats", err);
  }
}

// ===================== RENDER REFERRAL NETWORK =====================

function renderReferralNetwork(referrals) {
  const container = document.getElementById("referralList");
  if (!container) return;

  if (referrals.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>No referral contacts yet</h3>
        <p>Add people in your network who can refer you, then link them to a job.</p>
      </div>`;
    return;
  }

  container.innerHTML = referrals.map((ref) => {
    const initials = (ref.referrerName || "?")
      .split(" ").map((p) => p[0]).filter(Boolean).slice(0, 2).join("").toUpperCase();

    return `
      <div class="referral-card" data-id="${ref.id}">
        <div class="referral-card-head">
          <div class="referral-avatar">${escapeHtml(initials)}</div>
          <div>
            <h3>${escapeHtml(ref.referrerName)}</h3>
            ${ref.relationship ? `<div class="referral-relation">${formatLabel(ref.relationship)}</div>` : ""}
          </div>
        </div>
        <div class="referral-meta">
          ${ref.company ? `<span>🏢 ${escapeHtml(ref.company)}</span>` : ""}
          ${ref.referrerEmail ? `<span>✉️ ${escapeHtml(ref.referrerEmail)}</span>` : ""}
          ${ref.referrerPhone ? `<span>📞 ${escapeHtml(ref.referrerPhone)}</span>` : ""}
          ${ref.referrerLinkedinUrl ? `<span>🔗 <a href="${escapeHtml(ref.referrerLinkedinUrl)}" target="_blank" rel="noopener">LinkedIn</a></span>` : ""}
        </div>
        <span class="referral-status-badge ref-status-${(ref.status || "NOT_REQUESTED").toLowerCase().replace(/_/g, "-")}">
  ${formatLabel(ref.status || "NOT_REQUESTED")}
</span>
<span class="referral-job-count">${ref.referredJobCount || 0} job${ref.referredJobCount === 1 ? "" : "s"} linked</span>
        <span class="referral-job-count">${ref.referredJobCount || 0} job${ref.referredJobCount === 1 ? "" : "s"} linked</span>
        <div class="referral-card-actions">
          <button class="btn btn-secondary btn-sm" onclick="openReferralModal(${ref.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteReferral(${ref.id})">Delete</button>
        </div>
      </div>`;
  }).join("");
}

// ===================== MINI LIST IN SIDEBAR =====================

function renderReferralMiniList(referrals) {
  const container = document.getElementById("referralMiniList");
  if (!container) return;

  if (referrals.length === 0) {
    container.innerHTML = `<div style="font-size:12px;color:var(--text-dim);padding:8px 0;">No contacts yet — add one in Referrals tab.</div>`;
    return;
  }

  const STATUS_MAP = {
    REQUESTED: { cls: "dot-yellow", label: "Requested", color: "#854F0B" },
    REFERRED: { cls: "dot-green", label: "Secured", color: "#0F6E56" },
    NO_RESPONSE: { cls: "dot-gray", label: "No response", color: "#888780" },
    DECLINED: { cls: "dot-red", label: "Declined", color: "#A32D2D" },
    NOT_REQUESTED: { cls: "dot-gray", label: "Pending", color: "#888780" },
  };

  container.innerHTML = referrals.slice(0, 5).map((ref) => {
    const initials = (ref.referrerName || "?")
      .split(" ").map((p) => p[0]).filter(Boolean).slice(0, 2).join("").toUpperCase();
    const s = STATUS_MAP[ref.status || "NOT_REQUESTED"] || STATUS_MAP.NOT_REQUESTED;

    return `
      <div class="ref-mini-item">
        <div class="ref-mini-person">
          <div class="ref-mini-avatar">${escapeHtml(initials)}</div>
          <div>
            <div class="ref-mini-name">${escapeHtml(ref.referrerName)}</div>
            <div class="ref-mini-role">${ref.company ? escapeHtml(ref.company) : "—"}</div>
          </div>
        </div>
        <div class="ref-mini-status">
          <div class="ref-status-dot ${s.cls}"></div>
          <span style="font-size:11px;color:${s.color}">${s.label}</span>
        </div>
      </div>`;
  }).join("");
}

// ===================== REFERRAL MODAL =====================

function openReferralModal(referralId = null) {
  editingReferralId = referralId;
  document.getElementById("referralForm").reset();

  const relSelect = document.getElementById("ref_relationship");
  relSelect.innerHTML =
    `<option value="">Select relationship</option>` +
    RELATIONSHIP_VALUES.map((v) => `<option value="${v}">${formatLabel(v)}</option>`).join("");

  document.getElementById("referralModalTitle").textContent =
    referralId ? "Edit Referral Contact" : "Add Referral Contact";

  if (referralId) {
    const ref = allReferrals.find((r) => r.id === referralId);
    if (ref) {
      document.getElementById("ref_referrerName").value = ref.referrerName || "";
      document.getElementById("ref_relationship").value = ref.relationship || "";
      document.getElementById("ref_company").value = ref.company || "";
      document.getElementById("ref_referrerEmail").value = ref.referrerEmail || "";
      document.getElementById("ref_referrerPhone").value = ref.referrerPhone || "";
      document.getElementById("ref_referrerLinkedinUrl").value = ref.referrerLinkedinUrl || "";
      document.getElementById("ref_notes").value = ref.notes || "";

      document.getElementById("ref_status").value = ref.status || "NOT_REQUESTED";


    }
  }

  document.getElementById("referralModalOverlay").classList.add("visible");
}

function closeReferralModal() {
  document.getElementById("referralModalOverlay").classList.remove("visible");
  editingReferralId = null;
}

async function handleSaveReferral(event) {
  event.preventDefault();
  const saveBtn = document.getElementById("referralSaveBtn");

  const payload = {
    referrerName: document.getElementById("ref_referrerName").value.trim(),
    relationship: document.getElementById("ref_relationship").value || null,
    company: document.getElementById("ref_company").value.trim() || null,
    referrerEmail: document.getElementById("ref_referrerEmail").value.trim() || null,
    referrerPhone: document.getElementById("ref_referrerPhone").value.trim() || null,
    referrerLinkedinUrl: document.getElementById("ref_referrerLinkedinUrl").value.trim() || null,
    notes: document.getElementById("ref_notes").value.trim() || null,
    status: document.getElementById("ref_status").value || "NOT_REQUESTED", // ✅
  };

  if (!payload.referrerName) {
    showToast("Referrer name is required.", "error");
    return;
  }

  setButtonLoading(saveBtn, true, "Saving...");

  try {
    if (editingReferralId) {
      await apiFetch(`/referrals/${editingReferralId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      showToast("Referral contact updated.");
    } else {
      await apiFetch("/referrals", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      showToast("Referral contact added.");
    }

    closeReferralModal();
    await loadReferrals();
    await loadReferralStats();
    if (typeof loadDashboard === "function") loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to save referral contact.", "error");
  } finally {
    setButtonLoading(saveBtn, false);
  }
}

async function handleDeleteReferral(referralId) {
  if (!confirm("Delete this referral contact? Jobs linked to them will be unlinked.")) return;
  try {
    await apiFetch(`/referrals/${referralId}`, { method: "DELETE" });
    showToast("Referral contact deleted.");
    await loadReferrals();
    await loadReferralStats();
    if (typeof loadDashboard === "function") loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to delete referral contact.", "error");
  }
}

// ===================== RESUME VAULT SIDEBAR =====================

async function loadSidebarResumes() {
  const container = document.getElementById("resumeVaultList");
  if (!container) return;
  try {
    const token = Auth.getToken();
    const res = await fetch("/api/resumes", {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) { container.innerHTML = ""; return; }
    const resumes = await res.json();
    if (!resumes || resumes.length === 0) { container.innerHTML = ""; return; }

    container.innerHTML = resumes.map((r) => `
      <div class="resume-file-item">
        <div class="resume-file-icon">📄</div>
        <div style="flex:1;min-width:0;">
          <div class="resume-file-name">${escapeHtml(r.label || r.cvFileName || "Resume")}</div>
          <div class="resume-file-meta">${r.cvFileName ? escapeHtml(r.cvFileName) : ""}${r.version ? ` · ${escapeHtml(r.version)}` : ""}</div>
        </div>
        <div class="resume-file-actions">
<button class="btn btn-secondary btn-sm" onclick="viewResume(${r.id})" title="View">⬇</button>
        </div>
      </div>`).join("");
  } catch (err) {
    console.error("Failed to load sidebar resumes", err);
  }
}

async function handleQuickResumeUpload(e) {
  const file = e.target.files[0];
  if (!file) return;
  const statusEl = document.getElementById("quickUploadStatus");
  statusEl.textContent = "Uploading...";
  statusEl.style.color = "var(--text-dim)";
  try {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("label", file.name.replace(/\.[^.]+$/, ""));
    const token = Auth.getToken();
    const res = await fetch("/api/resumes/upload", {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    });
    if (!res.ok) throw new Error();
    statusEl.textContent = "✅ Uploaded";
    statusEl.style.color = "#4cb782";
    loadSidebarResumes();
    setTimeout(() => { statusEl.textContent = ""; }, 3000);
  } catch {
    statusEl.textContent = "⚠️ Upload failed";
    statusEl.style.color = "#e25c5c";
  }
  e.target.value = "";
}

// ===================== SHARED HELPERS =====================

// Called by jobs.js to populate referral dropdown inside job modal
function populateJobReferralSelect(selectedId = "") {
  const select = document.getElementById("job_referralId");
  if (!select) return;

  select.innerHTML =
    `<option value="">No referral for this application</option>` +
    allReferrals.map((ref) => {
      const label = [
        ref.referrerName,
        ref.relationship ? formatLabel(ref.relationship) : null,
        ref.company ? `@ ${ref.company}` : null,
      ].filter(Boolean).join(" — ");
      return `<option value="${ref.id}">${escapeHtml(label)}</option>`;
    }).join("");

  select.value = selectedId || "";
}

function getReferralById(id) {
  if (!id) return null;
  return allReferrals.find((r) => r.id === id) || null;
}

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