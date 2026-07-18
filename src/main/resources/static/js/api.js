// ===================== API base =====================
const API_BASE = '/api';

// ===================== Auth token helpers =====================
function getToken() {
  return localStorage.getItem('token');
}

function getRole() {
  return localStorage.getItem('role');
}

function getFullName() {
  return localStorage.getItem('fullName');
}

function saveSession(authResponse) {
  localStorage.setItem('token', authResponse.token);
  localStorage.setItem('role', authResponse.role);
  localStorage.setItem('fullName', authResponse.fullName);
  localStorage.setItem('email', authResponse.email);
}

function logout() {
  localStorage.clear();
  window.location.href = '/login.html';
}

// Redirects to the correct dashboard based on role - used after login
// and to guard pages that require a specific role.
function redirectToDashboard(role) {
  if (role === 'STUDENT') window.location.href = '/student/dashboard.html';
  else if (role === 'ADMIN') window.location.href = '/admin/dashboard.html';
  else window.location.href = '/login.html';
}

/**
 * M-15 / C-7: Parses the JWT payload and checks the exp claim client-side.
 * This is a UX improvement only — the server always re-validates the token.
 * Returns true if the token is present and not yet expired.
 */
function isTokenExpired() {
  const token = getToken();
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    // exp is in seconds; Date.now() is in ms
    return Date.now() >= payload.exp * 1000;
  } catch {
    return true; // malformed token = treat as expired
  }
}

// Call at the top of every protected page - kicks the user back to login
// if they don't have a valid, non-expired token for the required role.
function requireRole(expectedRole) {
  const token = getToken();
  const role = getRole();
  if (!token || role !== expectedRole || isTokenExpired()) {
    window.location.href = '/login.html';
  }
}

// ===================== Core fetch wrapper =====================
// Every authenticated call goes through this so we don't repeat
// header-building and error-handling logic in every page's JS file.
async function apiFetch(path, options = {}) {
  const headers = options.body instanceof FormData
    ? {}
    : { 'Content-Type': 'application/json' };

  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(API_BASE + path, {
    ...options,
    headers: { ...headers, ...(options.headers || {}) },
  });

  if (response.status === 401 || response.status === 403) {
    if (path !== '/auth/login' && path !== '/auth/register') {
      showToast('Session expired or access denied. Please log in again.', 'danger');
      setTimeout(logout, 1500);
    }
  }

  const contentType = response.headers.get('content-type') || '';
  const data = contentType.includes('application/json') ? await response.json() : null;

  if (!response.ok) {
    let message = data?.message || 'Something went wrong';
    if (data?.fieldErrors) {
      const fieldIssues = Object.values(data.fieldErrors).join('. ');
      message += `: ${fieldIssues}`;
    }
    throw new Error(message);
  }

  return data;
}

// ===================== Toast notifications =====================
// H-9: Uses textContent (not innerHTML) for the message body to prevent XSS.
function showToast(message, type = 'success') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    document.body.appendChild(container);
  }

  const toastEl = document.createElement('div');
  toastEl.className = `toast align-items-center text-bg-${type} border-0`;
  toastEl.setAttribute('role', 'alert');
  toastEl.setAttribute('aria-live', 'assertive');
  toastEl.setAttribute('aria-atomic', 'true');

  const dFlex = document.createElement('div');
  dFlex.className = 'd-flex';

  // H-9: textContent prevents HTML injection from server-controlled error messages
  const body = document.createElement('div');
  body.className = 'toast-body';
  body.textContent = message;

  const closeBtn = document.createElement('button');
  closeBtn.type = 'button';
  closeBtn.className = 'btn-close btn-close-white me-2 m-auto';
  closeBtn.setAttribute('data-bs-dismiss', 'toast');
  closeBtn.setAttribute('aria-label', 'Close');

  dFlex.appendChild(body);
  dFlex.appendChild(closeBtn);
  toastEl.appendChild(dFlex);
  container.appendChild(toastEl);

  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  toast.show();
  toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

// ===================== XSS-safe HTML escape =====================
// H-8: Use this for ALL user-supplied or server-returned text embedded in HTML templates.
function escHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;');
}

// ===================== Status badge helper =====================
function statusBadgeClass(status) {
  const map = {
    APPLIED: 'bg-secondary',
    SHORTLISTED: 'bg-warning text-dark',
    SELECTED: 'bg-success',
    REJECTED: 'bg-danger',
    OPEN: 'bg-success',
    UPCOMING: 'bg-info text-dark',
    CLOSED: 'bg-secondary',
  };
  return map[status] || 'bg-secondary';
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' });
}

// ===================== Loading spinner helpers =====================
// L-9: Show/hide a spinner element by ID for better perceived performance.
function showSpinner(containerId) {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = `
    <div class="d-flex justify-content-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>`;
}

function showEmptyState(containerId, message) {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = `<p class="text-muted text-center py-4">${escHtml(message)}</p>`;
}
