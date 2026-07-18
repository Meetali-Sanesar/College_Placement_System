// ===== Student Dashboard JS =====
requireRole('STUDENT');

const name = getFullName();
document.getElementById('sidebarUser').textContent = name || '';

// ===================== Tab navigation =====================
function showTab(tab) {
  ['drives', 'applications', 'profile', 'analyzer'].forEach(t => {
    document.getElementById('tab' + capitalize(t)).classList.toggle('active', t === tab);
    const nav = document.getElementById('nav' + capitalize(t));
    if (nav) nav.classList.toggle('active', t === tab);
  });
  if (tab === 'drives') loadDrives();
  if (tab === 'applications') loadApplications();
  if (tab === 'profile') loadProfile();
}

function capitalize(s) { return s.charAt(0).toUpperCase() + s.slice(1); }

// ===================== Drives =====================
async function loadDrives() {
  const container = document.getElementById('drivesContainer');
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  try {
    const data = await apiFetch('/drives?page=0&size=50');
    const drives = data.content || [];
    if (!drives.length) { container.innerHTML = '<p class="text-muted">No open placement drives at the moment.</p>'; return; }
    
    let appliedDriveIds = new Set();
    try {
      const myApps = await apiFetch('/applications/my?page=0&size=500');
      (myApps.content || []).forEach(a => appliedDriveIds.add(a.driveId));
    } catch(e) {
      console.warn("Could not fetch user applications for drive status", e);
    }

    container.innerHTML = drives.map(d => renderDriveCard(d, appliedDriveIds.has(d.id))).join('');
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

function renderDriveCard(d, hasApplied) {
  const branch = d.eligibleBranches ? d.eligibleBranches : 'All branches';
  const cgpa = d.eligibilityCgpa ? `CGPA ≥ ${d.eligibilityCgpa}` : 'No CGPA bar';
  
  let actionButton = '';
  if (hasApplied) {
    actionButton = `<button class="btn btn-sm btn-success" disabled><i class="bi bi-check-circle me-1"></i>Applied</button>`;
  } else {
    actionButton = `<button class="btn btn-sm btn-primary" onclick="applyToDrive(${d.id}, this)"><i class="bi bi-send me-1"></i>Apply Now</button>`;
  }

  return `
  <div class="card drive-card mb-3 shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-start">
        <div>
          <h5 class="fw-bold mb-1">${escHtml(d.role)}</h5>
          <p class="text-muted mb-1"><i class="bi bi-building me-1"></i>${escHtml(d.companyName)}</p>
        </div>
        <span class="badge ${statusBadgeClass(d.status)}">${escHtml(d.status)}</span>
      </div>
      <div class="small text-muted mb-2">
        <div id="descPreview${d.id}" style="white-space:pre-line">${escHtml(d.description.substring(0, 150))}${d.description.length > 150 ? '...' : ''}</div>
        <div class="collapse" id="descFull${d.id}" style="white-space:pre-line">${escHtml(d.description)}</div>
        ${d.description.length > 150 ? `<a class="text-primary mt-1 d-inline-block" style="cursor:pointer; text-decoration:none;" onclick="toggleDesc(${d.id}, this)">Read More</a>` : ''}
      </div>
      <div class="row g-2 small text-muted mb-3">
        ${d.packageLpa ? `<div class="col-auto"><i class="bi bi-currency-rupee"></i>${escHtml(String(d.packageLpa))} LPA</div>` : ''}
        ${d.location ? `<div class="col-auto"><i class="bi bi-geo-alt"></i> ${escHtml(d.location)}</div>` : ''}
        <div class="col-auto"><i class="bi bi-people"></i> ${escHtml(branch)}</div>
        <div class="col-auto"><i class="bi bi-mortarboard"></i> ${escHtml(cgpa)}</div>
        ${d.deadline ? `<div class="col-auto"><i class="bi bi-calendar"></i> Deadline: ${formatDate(d.deadline)}</div>` : ''}
      </div>
      ${actionButton}
    </div>
  </div>`;
}

function toggleDesc(id, btn) {
  const preview = document.getElementById('descPreview' + id);
  const full = document.getElementById('descFull' + id);
  if (full.classList.contains('show')) {
    full.classList.remove('show');
    preview.style.display = 'block';
    btn.textContent = 'Read More';
  } else {
    full.classList.add('show');
    preview.style.display = 'none';
    btn.textContent = 'Read Less';
  }
}

async function applyToDrive(driveId, btn) {
  btn.disabled = true;
  btn.textContent = 'Applying...';
  try {
    await apiFetch(`/applications/apply/${driveId}`, { method: 'POST' });
    showToast('Application submitted successfully!', 'success');
    btn.textContent = '✓ Applied';
    btn.classList.replace('btn-primary', 'btn-success');
  } catch (err) {
    showToast(err.message, 'danger');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-send me-1"></i>Apply Now';
  }
}

// ===================== Applications =====================
async function loadApplications() {
  const container = document.getElementById('appsContainer');
  container.innerHTML = '<p class="text-muted">Loading...</p>';
  try {
    const data = await apiFetch('/applications/my?page=0&size=50');
    const apps = data.content || [];
    if (!apps.length) { container.innerHTML = '<p class="text-muted">You haven\'t applied to any drives yet.</p>'; return; }
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead class="table-light"><tr>
            <th>Company</th><th>Role</th><th>Applied On</th><th>Status</th>
          </tr></thead>
          <tbody>
            ${apps.map(a => `<tr>
              <td><strong>${escHtml(a.companyName)}</strong></td>
              <td>${escHtml(a.driveRole)}</td>
              <td>${formatDate(a.appliedAt)}</td>
              <td><span class="badge ${statusBadgeClass(a.status)}">${escHtml(a.status)}</span></td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${escHtml(err.message)}</div>`;
  }
}

// ===================== Profile =====================
async function loadProfile() {
  try {
    const p = await apiFetch('/students/profile');
    document.getElementById('pCollege').value = p.college || '';
    document.getElementById('pBranch').value = p.branch || '';
    document.getElementById('pGradYear').value = p.graduationYear || '';
    document.getElementById('pCgpa').value = p.cgpa || '';
    document.getElementById('pPhone').value = p.phone || '';
    document.getElementById('pSkills').value = p.skills || '';
    document.getElementById('resumeStatus').innerHTML = p.resumeUrl
      ? `<a href="${escHtml(p.resumeUrl)}" target="_blank" class="text-success"><i class="bi bi-check-circle me-1"></i>Resume uploaded</a>`
      : '<span class="text-warning"><i class="bi bi-exclamation-circle me-1"></i>No resume uploaded yet</span>';
  } catch (err) {
    showToast('Failed to load profile: ' + err.message, 'danger');
  }
}

document.getElementById('profileForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = document.getElementById('saveProfileBtn');
  btn.disabled = true; btn.textContent = 'Saving...';
  try {
    await apiFetch('/students/profile', {
      method: 'PUT',
      body: JSON.stringify({
        college: document.getElementById('pCollege').value.trim(),
        branch: document.getElementById('pBranch').value.trim(),
        graduationYear: parseInt(document.getElementById('pGradYear').value) || null,
        cgpa: parseFloat(document.getElementById('pCgpa').value) || null,
        phone: document.getElementById('pPhone').value.trim(),
        skills: document.getElementById('pSkills').value.trim(),
      }),
    });
    showToast('Profile updated!', 'success');
  } catch (err) {
    showToast(err.message, 'danger');
  } finally {
    btn.disabled = false; btn.textContent = 'Save Profile';
  }
});

document.getElementById('resumeForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const file = document.getElementById('resumeFile').files[0];
  if (!file) { showToast('Please select a PDF file', 'warning'); return; }
  const btn = document.getElementById('uploadResumeBtn');
  btn.disabled = true; btn.textContent = 'Uploading...';
  const fd = new FormData();
  fd.append('file', file);
  try {
    await apiFetch('/students/resume', { method: 'POST', body: fd });
    showToast('Resume uploaded successfully!', 'success');
    loadProfile();
  } catch (err) {
    showToast(err.message, 'danger');
  } finally {
    btn.disabled = false; btn.textContent = 'Upload Resume (PDF, max 5MB)';
  }
});

// ===================== Resume Analyzer =====================
document.getElementById('analyzerForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const file = document.getElementById('analyzerFile').files[0];
  const jd = document.getElementById('analyzerJd').value.trim();
  if (!file) { showToast('Please select a resume PDF', 'warning'); return; }
  if (!jd) { showToast('Please paste the job description', 'warning'); return; }

  const btn = document.getElementById('analyzeBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Analyzing...';

  const fd = new FormData();
  fd.append('resume', file);
  fd.append('jobDescription', jd);

  try {
    const result = await apiFetch('/resume-analysis/analyze', { method: 'POST', body: fd });
    renderAnalysisResult(result);
  } catch (err) {
    showToast(err.message, 'danger');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-search me-2"></i>Analyze Resume';
  }
});

function renderAnalysisResult(r) {
  const placeholder = document.getElementById('analysisPlaceholder');
  const result = document.getElementById('analysisResult');
  placeholder.classList.add('d-none');
  result.classList.remove('d-none');

  const scoreColor = r.atsScore >= 75 ? 'text-success' : r.atsScore >= 50 ? 'text-warning' : 'text-danger';

  result.innerHTML = `
    <div class="card shadow-sm p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h5 class="fw-bold mb-0">Analysis Results</h5>
        <span class="analysis-badge">${escHtml(r.badge)}</span>
      </div>

      <!-- ATS Score -->
      <div class="text-center mb-4">
        <div class="display-4 fw-bold ${scoreColor}">${r.atsScore}%</div>
        <div class="text-muted small">Estimated ATS Match Score</div>
        <div class="progress mt-2" style="height:10px;border-radius:10px;">
          <div class="progress-bar ${r.atsScore>=75?'bg-success':r.atsScore>=50?'bg-warning':'bg-danger'}"
               style="width:${r.atsScore}%"></div>
        </div>
      </div>

      <!-- Matching Skills -->
      <div class="mb-3">
        <strong class="d-block mb-2"><i class="bi bi-check-circle-fill text-success me-1"></i>Matching Skills</strong>
        <div class="d-flex flex-wrap gap-2">
          ${(r.matchingSkills||[]).map(s=>`<span class="badge bg-success-subtle text-success border border-success">${escHtml(s)}</span>`).join('')}
        </div>
      </div>

      <!-- Missing Keywords -->
      <div class="mb-3">
        <strong class="d-block mb-2"><i class="bi bi-x-circle-fill text-danger me-1"></i>Missing Keywords</strong>
        <div class="d-flex flex-wrap gap-2">
          ${(r.missingKeywords||[]).map(s=>`<span class="badge bg-danger-subtle text-danger border border-danger">${escHtml(s)}</span>`).join('')}
        </div>
      </div>

      <!-- Suggestions -->
      <div class="mb-3">
        <strong class="d-block mb-2"><i class="bi bi-lightbulb-fill text-warning me-1"></i>Improvement Suggestions</strong>
        <ul class="list-group list-group-flush">
          ${(r.suggestions||[]).map(s=>`<li class="list-group-item px-0">${escHtml(s)}</li>`).join('')}
        </ul>
      </div>

      <!-- Skills to Learn -->
      <div class="mb-3">
        <strong class="d-block mb-2"><i class="bi bi-mortarboard-fill text-primary me-1"></i>Skills to Learn</strong>
        <div class="d-flex flex-wrap gap-2">
          ${(r.skillsToLearn||[]).map(s=>`<span class="badge bg-primary-subtle text-primary border border-primary">${escHtml(s)}</span>`).join('')}
        </div>
      </div>

      <!-- Section Feedback -->
      ${r.sectionFeedback && r.sectionFeedback.length ? `
      <div class="mb-0">
        <strong class="d-block mb-2"><i class="bi bi-file-earmark-text-fill text-info me-1"></i>Section-wise Feedback</strong>
        ${r.sectionFeedback.map(sf=>`
          <div class="section-feedback-item">
            <div class="fw-semibold small text-primary">${escHtml(sf.section)}</div>
            <div class="small text-muted">${escHtml(sf.feedback)}</div>
          </div>`).join('')}
      </div>` : ''}
    </div>`;
}

// ===================== Init =====================
showTab('drives');
