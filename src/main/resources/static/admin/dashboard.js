// ===== Admin Dashboard JS =====
requireRole('ADMIN');

let companyModalInstance, driveModalInstance;

window.addEventListener('load', () => {
  companyModalInstance = new bootstrap.Modal(document.getElementById('companyModal'));
  driveModalInstance   = new bootstrap.Modal(document.getElementById('driveModal'));
  showTab('stats');
});

// ===================== Tab navigation =====================
function showTab(tab) {
  ['stats','students','companies','drives','applications'].forEach(t => {
    document.getElementById('tab' + capitalize(t)).classList.toggle('active', t === tab);
    const nav = document.getElementById('nav' + capitalize(t));
    if (nav) nav.classList.toggle('active', t === tab);
  });
  if (tab === 'stats')        loadStats();
  if (tab === 'students')     loadStudents();
  if (tab === 'companies')    loadCompanies();
  if (tab === 'drives')       loadDrives();
  if (tab === 'applications') loadApplications();
}

function capitalize(s) { return s.charAt(0).toUpperCase() + s.slice(1); }

// ===================== Stats =====================
async function loadStats() {
  const container = document.getElementById('statsContainer');
  try {
    const s = await apiFetch('/admin/stats');
    container.innerHTML = `
      ${statCard('Students', s.totalStudents, 'bi-people-fill', '#0d6efd')}
      ${statCard('Companies', s.totalCompanies, 'bi-building-fill', '#6610f2')}
      ${statCard('Total Drives', s.totalDrives, 'bi-briefcase-fill', '#198754')}
      ${statCard('Open Drives', s.openDrives, 'bi-door-open-fill', '#0dcaf0')}
      ${statCard('Applications', s.totalApplications, 'bi-file-earmark-text-fill', '#fd7e14')}
      ${statCard('Shortlisted', s.shortlisted, 'bi-star-fill', '#ffc107')}
      ${statCard('Selected', s.selected, 'bi-trophy-fill', '#20c997')}
    `;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger col-12">${escHtml(err.message)}</div>`;
  }
}

function statCard(label, value, icon, color) {
  return `
    <div class="col-sm-6 col-lg-3">
      <div class="card stat-card shadow-sm p-3 d-flex flex-row align-items-center gap-3">
        <div class="stat-icon" style="background:${color}22;color:${color}">
          <i class="bi ${icon}"></i>
        </div>
        <div>
          <div class="fw-bold fs-4">${value}</div>
          <div class="text-muted small">${label}</div>
        </div>
      </div>
    </div>`;
}

// ===================== Students =====================
async function loadStudents() {
  const container = document.getElementById('studentsContainer');
  const keyword = document.getElementById('studentSearch').value.trim();
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  try {
    const url = keyword ? `/admin/students?keyword=${encodeURIComponent(keyword)}&size=50` : '/admin/students?size=50';
    const data = await apiFetch(url);
    const items = data.content || [];
    if (!items.length) { container.innerHTML = '<p class="text-muted">No students found.</p>'; return; }
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light"><tr>
            <th>Name</th><th>Email</th><th>Branch</th><th>CGPA</th><th>Year</th><th>Action</th>
          </tr></thead>
          <tbody>
            ${items.map(s => `<tr>
              <td>${escHtml(s.fullName)}</td>
              <td>${escHtml(s.email)}</td>
              <td>${escHtml(s.branch||'—')}</td>
              <td>${s.cgpa != null ? s.cgpa : '—'}</td>
              <td>${s.graduationYear||'—'}</td>
              <td><button class="btn btn-sm btn-outline-danger" onclick="deleteStudent(${s.id}, this)">Delete</button></td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

document.getElementById('studentSearch').addEventListener('keyup', e => { if (e.key === 'Enter') loadStudents(); });

async function deleteStudent(id, btn) {
  if (!confirm('Delete this student account and all their applications?')) return;
  btn.disabled = true;
  try {
    await apiFetch(`/admin/students/${id}`, { method: 'DELETE' });
    showToast('Student deleted.', 'success');
    loadStudents();
  } catch (err) { showToast(err.message, 'danger'); btn.disabled = false; }
}

// ===================== Companies =====================
let companiesCache = [];

async function loadCompanies() {
  const container = document.getElementById('companiesContainer');
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  try {
    const data = await apiFetch('/admin/companies?size=100');
    companiesCache = data.content || [];
    if (!companiesCache.length) { container.innerHTML = '<p class="text-muted">No companies yet. Add one to get started.</p>'; return; }
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light"><tr>
            <th>Name</th><th>Industry</th><th>Location</th><th>Contact</th><th>Actions</th>
          </tr></thead>
          <tbody>
            ${companiesCache.map(c => `<tr>
              <td><strong>${escHtml(c.name)}</strong>${c.website?`<br><a href="${escHtml(c.website)}" target="_blank" class="small text-muted">${escHtml(c.website)}</a>`:''}</td>
              <td>${escHtml(c.industry||'—')}</td>
              <td>${escHtml(c.location||'—')}</td>
              <td>${escHtml(c.contactEmail||'')}${c.contactPhone?`<br>${escHtml(c.contactPhone)}`:''}</td>
              <td class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary" onclick='editCompany(${JSON.stringify(c)})'>Edit</button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteCompany(${c.id}, this)">Delete</button>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

function openCompanyModal() {
  document.getElementById('companyModalTitle').textContent = 'Add Company';
  document.getElementById('companyForm').reset();
  document.getElementById('companyId').value = '';
  companyModalInstance.show();
}

function editCompany(c) {
  document.getElementById('companyModalTitle').textContent = 'Edit Company';
  document.getElementById('companyId').value = c.id;
  document.getElementById('cName').value = c.name || '';
  document.getElementById('cIndustry').value = c.industry || '';
  document.getElementById('cWebsite').value = c.website || '';
  document.getElementById('cLocation').value = c.location || '';
  document.getElementById('cEmail').value = c.contactEmail || '';
  document.getElementById('cPhone').value = c.contactPhone || '';
  document.getElementById('cDesc').value = c.description || '';
  companyModalInstance.show();
}

document.getElementById('companyForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('companyId').value;
  const btn = document.getElementById('saveCompanyBtn');
  btn.disabled = true; btn.textContent = 'Saving...';
  const payload = {
    name: document.getElementById('cName').value.trim(),
    industry: document.getElementById('cIndustry').value.trim(),
    website: document.getElementById('cWebsite').value.trim(),
    location: document.getElementById('cLocation').value.trim(),
    contactEmail: document.getElementById('cEmail').value.trim(),
    contactPhone: document.getElementById('cPhone').value.trim(),
    description: document.getElementById('cDesc').value.trim(),
  };
  try {
    if (id) { await apiFetch(`/admin/companies/${id}`, { method: 'PUT', body: JSON.stringify(payload) }); }
    else     { await apiFetch('/admin/companies', { method: 'POST', body: JSON.stringify(payload) }); }
    companyModalInstance.hide();
    showToast('Company saved!', 'success');
    loadCompanies();
  } catch (err) { showToast(err.message, 'danger'); }
  finally { btn.disabled = false; btn.textContent = 'Save'; }
});

async function deleteCompany(id, btn) {
  if (!confirm('Delete this company? Drives linked to it must be deleted first.')) return;
  btn.disabled = true;
  try {
    await apiFetch(`/admin/companies/${id}`, { method: 'DELETE' });
    showToast('Company deleted.', 'success');
    loadCompanies();
  } catch (err) { showToast(err.message, 'danger'); btn.disabled = false; }
}

// ===================== Drives =====================
async function loadDrives() {
  const container = document.getElementById('drivesContainer');
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  // Ensure companies are cached for the modal
  if (!companiesCache.length) { await loadCompaniesForCache(); }
  try {
    const data = await apiFetch('/admin/drives?size=100');
    const drives = data.content || [];
    if (!drives.length) { container.innerHTML = '<p class="text-muted">No drives yet. Create one to get started.</p>'; return; }
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light"><tr>
            <th>Company</th><th>Role</th><th>Package</th><th>Deadline</th><th>Status</th><th>Actions</th>
          </tr></thead>
          <tbody>
            ${drives.map(d => `<tr>
              <td>${escHtml(d.companyName)}</td>
              <td><strong>${escHtml(d.role)}</strong></td>
              <td>${d.packageLpa ? d.packageLpa + ' LPA' : '—'}</td>
              <td>${d.deadline ? formatDate(d.deadline) : '—'}</td>
              <td><span class="badge ${statusBadgeClass(d.status)}">${escHtml(d.status)}</span></td>
              <td class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary" onclick='editDrive(${JSON.stringify(d)})'>Edit</button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteDrive(${d.id}, this)">Delete</button>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

async function loadCompaniesForCache() {
  try {
    const data = await apiFetch('/admin/companies?size=200');
    companiesCache = data.content || [];
  } catch {}
}

function openDriveModal() {
  document.getElementById('driveModalTitle').textContent = 'Create Placement Drive';
  document.getElementById('driveForm').reset();
  document.getElementById('driveId').value = '';
  populateCompanyDropdown();
  driveModalInstance.show();
}

function editDrive(d) {
  document.getElementById('driveModalTitle').textContent = 'Edit Placement Drive';
  document.getElementById('driveId').value = d.id;
  populateCompanyDropdown(d.companyId);
  document.getElementById('dRole').value = d.role || '';
  document.getElementById('dDesc').value = d.description || '';
  document.getElementById('dCgpa').value = d.eligibilityCgpa || '';
  document.getElementById('dBranches').value = d.eligibleBranches || '';
  document.getElementById('dPackage').value = d.packageLpa || '';
  document.getElementById('dDriveDate').value = d.driveDate || '';
  document.getElementById('dDeadline').value = d.deadline || '';
  document.getElementById('dLocation').value = d.location || '';
  document.getElementById('dStatus').value = d.status || 'OPEN';
  driveModalInstance.show();
}

function populateCompanyDropdown(selectedId) {
  const sel = document.getElementById('dCompanyId');
  sel.innerHTML = '<option value="">Select company...</option>' +
    companiesCache.map(c => `<option value="${c.id}" ${c.id == selectedId ? 'selected' : ''}>${escHtml(c.name)}</option>`).join('');
}

document.getElementById('driveForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('driveId').value;
  const btn = document.getElementById('saveDriveBtn');
  btn.disabled = true; btn.textContent = 'Saving...';
  const payload = {
    companyId: parseInt(document.getElementById('dCompanyId').value),
    role: document.getElementById('dRole').value.trim(),
    description: document.getElementById('dDesc').value.trim(),
    eligibilityCgpa: parseFloat(document.getElementById('dCgpa').value) || null,
    eligibleBranches: document.getElementById('dBranches').value.trim() || null,
    packageLpa: parseFloat(document.getElementById('dPackage').value) || null,
    driveDate: document.getElementById('dDriveDate').value || null,
    deadline: document.getElementById('dDeadline').value || null,
    location: document.getElementById('dLocation').value.trim() || null,
    status: document.getElementById('dStatus').value,
  };
  try {
    if (id) { await apiFetch(`/admin/drives/${id}`, { method: 'PUT', body: JSON.stringify(payload) }); }
    else     { await apiFetch('/admin/drives', { method: 'POST', body: JSON.stringify(payload) }); }
    driveModalInstance.hide();
    showToast('Drive saved!', 'success');
    loadDrives();
  } catch (err) { showToast(err.message, 'danger'); }
  finally { btn.disabled = false; btn.textContent = 'Save'; }
});

async function deleteDrive(id, btn) {
  if (!confirm('Delete this drive and all its applications?')) return;
  btn.disabled = true;
  try {
    await apiFetch(`/admin/drives/${id}`, { method: 'DELETE' });
    showToast('Drive deleted.', 'success');
    loadDrives();
  } catch (err) { showToast(err.message, 'danger'); btn.disabled = false; }
}

// ===================== Applications =====================
async function loadApplications() {
  const container = document.getElementById('applicationsContainer');
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  try {
    const data = await apiFetch('/admin/applications?size=100');
    const apps = data.content || [];
    if (!apps.length) { container.innerHTML = '<p class="text-muted">No applications yet.</p>'; return; }
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light"><tr>
            <th>Student</th><th>Company</th><th>Role</th><th>Applied</th><th>Status</th><th>Update</th>
          </tr></thead>
          <tbody>
            ${apps.map(a => `<tr id="appRow${a.id}">
              <td>${escHtml(a.studentName)}</td>
              <td>${escHtml(a.companyName)}</td>
              <td>${escHtml(a.driveRole)}</td>
              <td>${formatDate(a.appliedAt)}</td>
              <td><span class="badge ${statusBadgeClass(a.status)}" id="badge${a.id}">${escHtml(a.status)}</span></td>
              <td>
                <select class="form-select form-select-sm" id="statusSel${a.id}" style="width:130px">
                  <option value="APPLIED" ${a.status==='APPLIED'?'selected':''}>Applied</option>
                  <option value="SHORTLISTED" ${a.status==='SHORTLISTED'?'selected':''}>Shortlisted</option>
                  <option value="SELECTED" ${a.status==='SELECTED'?'selected':''}>Selected</option>
                  <option value="REJECTED" ${a.status==='REJECTED'?'selected':''}>Rejected</option>
                </select>
                <button class="btn btn-sm btn-outline-primary ms-1 mt-1" onclick="updateStatus(${a.id})">Update</button>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

async function updateStatus(appId) {
  const newStatus = document.getElementById('statusSel' + appId).value;
  try {
    await apiFetch(`/admin/applications/${appId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: newStatus }),
    });
    showToast('Status updated!', 'success');
    const badge = document.getElementById('badge' + appId);
    badge.textContent = newStatus;
    badge.className = 'badge ' + statusBadgeClass(newStatus);
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

// Init
showTab('stats');
