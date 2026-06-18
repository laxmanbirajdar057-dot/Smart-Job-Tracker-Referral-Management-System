// Handles both the login page and the signup page.
// Which form is present on the page determines which handler attaches.

document.addEventListener("DOMContentLoaded", () => {
  // If already logged in, skip straight to the dashboard.
  if (Auth.isLoggedIn()) {
    window.location.href = "/api/dashboard";
    return;
  }

  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", handleLogin);
  }

  const signupForm = document.getElementById("signupForm");
  if (signupForm) {
    signupForm.addEventListener("submit", handleSignup);
  }
});

async function handleLogin(event) {
  event.preventDefault();
  hideError("errorBanner");

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const submitBtn = document.getElementById("submitBtn");

  if (!email || !password) {
    showError("errorBanner", "Please enter both email and password.");
    return;
  }

  setButtonLoading(submitBtn, true, "Logging in...");

  try {
    const data = await apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    Auth.setToken(data.token);
    Auth.setUser({
      id: data.id,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
    });

    window.location.href = "/api/dashboard";
  } catch (err) {
    showError("errorBanner", err.message || "Login failed. Check your credentials.");
  } finally {
    setButtonLoading(submitBtn, false);
  }
}

async function handleSignup(event) {
  event.preventDefault();
  hideError("errorBanner");

  const firstName = document.getElementById("firstName").value.trim();
  const lastName = document.getElementById("lastName").value.trim();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const targetRoles = document.getElementById("targetRoles").value.trim();
  const targetLocations = document.getElementById("targetLocations").value.trim();
  const submitBtn = document.getElementById("submitBtn");

  if (!firstName || !lastName || !email || !password) {
    showError("errorBanner", "First name, last name, email, and password are required.");
    return;
  }

  if (password.length < 6) {
    showError("errorBanner", "Password should be at least 6 characters.");
    return;
  }

  setButtonLoading(submitBtn, true, "Creating account...");

  try {
    const data = await apiFetch("/auth/signup", {
      method: "POST",
      body: JSON.stringify({
        email,
        password,
        firstName,
        lastName,
        targetRoles: targetRoles || null,
        targetLocations: targetLocations || null,
      }),
    });

    Auth.setToken(data.token);
    Auth.setUser({
      id: data.id,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
    });

    window.location.href = "/api/dashboard";
  } catch (err) {
    showError("errorBanner", err.message || "Signup failed. Please try again.");
  } finally {
    setButtonLoading(submitBtn, false);
  }
}
