// Shared helpers: API base, token storage, fetch wrapper, toast notifications.
// Loaded before auth.js / jobs.js on every page.

const API_BASE = "";

const Auth = {
  getToken() {
    return localStorage.getItem("token");
  },
  setToken(token) {
    localStorage.setItem("token", token);
  },
  clearToken() {
    localStorage.removeItem("token");
  },
  setUser(user) {
    localStorage.setItem("user", JSON.stringify(user));
  },
  getUser() {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  },
  isLoggedIn() {
    return !!this.getToken();
  },

  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/login-page";
  },
};

// Central fetch wrapper: attaches the Authorization header, parses JSON,
// and throws an Error with the backend's message on non-2xx responses.
async function apiFetch(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = Auth.getToken();
  if (token) {
    headers["Authorization"] = "Bearer " + token;
  }

  const response = await fetch(API_BASE + path, {
    ...options,
    headers,
  });

  // 204 No Content (e.g. DELETE) has no body to parse
  if (response.status === 204) {
    return null;
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    let message =
      (data && typeof data === "object" && data.message) ||
      (typeof data === "string" && data) ||
      `Request failed (${response.status})`;

    // Surface field-level validation errors if present (400 from @Valid)
    if (data && typeof data === "object" && data.errors) {
      const fieldMessages = Object.values(data.errors).join(", ");
      if (fieldMessages) message = fieldMessages;
    }

    if (response.status === 401) {
      // Token missing/expired/invalid — kick back to login.
      Auth.logout();
    }

    throw new Error(message);
  }

  return data;
}

function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  if (!toast) return;
  toast.textContent = message;
  toast.className = type;
  toast.style.display = "block";
  clearTimeout(toast._hideTimer);
  toast._hideTimer = setTimeout(() => {
    toast.style.display = "none";
  }, 3500);
}

function showError(bannerId, message) {
  const el = document.getElementById(bannerId);
  if (!el) return;
  el.textContent = message;
  el.classList.add("visible");
}

function hideError(bannerId) {
  const el = document.getElementById(bannerId);
  if (!el) return;
  el.classList.remove("visible");
}

function setButtonLoading(button, loading, loadingText = "Please wait...") {
  if (!button) return;
  if (loading) {
    button.dataset.originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> ${loadingText}`;
  } else {
    button.disabled = false;
    button.innerHTML = button.dataset.originalText || button.innerHTML;
  }
}
