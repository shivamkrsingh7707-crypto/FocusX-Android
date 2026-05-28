const DEFAULT_SUBJECTS = ['Math', 'Science', 'English', 'SST', 'Hindi'];
const STORAGE_KEY = 'focusx_data';

let state = {
  subjects: [...DEFAULT_SUBJECTS],
  sessions: [],
  tests: [],
  dailyGoal: 120,
  darkMode: false,
};

let timer = {
  running: false,
  paused: false,
  startTime: null,
  elapsed: 0,
  interval: null,
  currentSubject: DEFAULT_SUBJECTS[0],
};

function init() {
  loadState();
  applyTheme();
  populateSubjectSelects();
  renderSubjectTags();
  updateTimerDisplay();
  updateDashboard();
  updateTestStats();
  renderTestList();
  renderChart('week');
  renderBreakdown();
  updateTodayStats();
  updateStreakDisplay();
  bindEvents();
}

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const saved = JSON.parse(raw);
      state.subjects = saved.subjects || [...DEFAULT_SUBJECTS];
      state.sessions = saved.sessions || [];
      state.tests = saved.tests || [];
      state.dailyGoal = saved.dailyGoal || 120;
      state.darkMode = saved.darkMode || false;
    }
  } catch (e) {
    console.warn('Failed to load state:', e);
  }
}

function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    console.warn('Failed to save state:', e);
  }
}

function applyTheme() {
  document.documentElement.setAttribute('data-theme', state.darkMode ? 'dark' : 'light');
  document.getElementById('themeToggle').textContent = state.darkMode ? '☀️' : '🌙';
}

function populateSubjectSelects() {
  const selects = ['timerSubject', 'testSubject', 'testFilter'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    const current = el.value;
    el.innerHTML = state.subjects.map(s => `<option value="${s}">${s}</option>`).join('');
    if (state.subjects.includes(current)) el.value = current;
    if (id === 'testFilter') {
      el.innerHTML = '<option value="all">All Subjects</option>' + state.subjects.map(s => `<option value="${s}">${s}</option>`).join('');
    }
  });
}

function renderSubjectTags() {
  const container = document.getElementById('subjectList');
  container.innerHTML = state.subjects.map(s => `
    <span class="subject-tag">
      ${s}
      <span class="remove" data-subject="${s}">✕</span>
    </span>
  `).join('');
}

function updateTimerDisplay() {
  const totalMs = timer.elapsed;
  const hrs = Math.floor(totalMs / 3600000);
  const mins = Math.floor((totalMs % 3600000) / 60000);
  const secs = Math.floor((totalMs % 60000) / 1000);
  document.getElementById('timerDisplay').textContent =
    `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function getToday() {
  return new Date().toISOString().split('T')[0];
}

function getMinutesToday() {
  const today = getToday();
  return state.sessions
    .filter(s => s.date === today)
    .reduce((sum, s) => sum + s.duration, 0);
}

function getTodaySessions() {
  const today = getToday();
  return state.sessions.filter(s => s.date === today);
}

function getMinutesBySubject(date) {
  const records = state.sessions.filter(s => s.date === date);
  const map = {};
  records.forEach(r => {
    map[r.subject] = (map[r.subject] || 0) + r.duration;
  });
  return map;
}

function updateTodayStats() {
  const mins = getMinutesToday();
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  document.getElementById('todayMinutes').textContent =
    h > 0 ? `${h}h ${m}m` : `${m} min`;
  const goal = state.dailyGoal;
  const focus = goal > 0 ? Math.min(100, Math.round((mins / goal) * 100)) : 0;
  document.getElementById('focusScore').textContent = `${focus}%`;
}

function updateStreakDisplay() {
  const streak = calculateStreak();
  document.getElementById('streakDisplay').textContent = `🔥 ${streak}`;
}

function startTimer() {
  if (timer.running && !timer.paused) return;
  const subject = document.getElementById('timerSubject').value;
  timer.currentSubject = subject;

  if (!timer.running) {
    timer.startTime = Date.now();
    timer.elapsed = 0;
    timer.running = true;
    timer.paused = false;
  } else if (timer.paused) {
    timer.startTime = Date.now() - timer.elapsed;
    timer.paused = false;
  }

  timer.interval = setInterval(() => {
    timer.elapsed = Date.now() - timer.startTime;
    updateTimerDisplay();
  }, 100);

  document.getElementById('timerStart').disabled = true;
  document.getElementById('timerPause').disabled = false;
  document.getElementById('timerSubject').disabled = true;
  document.getElementById('timerStatus').textContent = `Studying ${timer.currentSubject}...`;
}

function pauseTimer() {
  if (!timer.running || timer.paused) return;
  clearInterval(timer.interval);
  timer.paused = true;
  timer.elapsed = Date.now() - timer.startTime;

  document.getElementById('timerStart').disabled = false;
  document.getElementById('timerPause').disabled = true;
  document.getElementById('timerStatus').textContent = 'Paused';
}

function resetTimer() {
  clearInterval(timer.interval);
  const wasRunning = timer.running && timer.elapsed > 0;
  const savedDuration = Math.round(timer.elapsed / 60000);

  if (wasRunning && savedDuration >= 1) {
    state.sessions.push({
      subject: timer.currentSubject,
      date: getToday(),
      duration: savedDuration,
      timestamp: Date.now(),
    });
    saveState();
    updateDashboard();
    renderBreakdown();
    updateTodayStats();
    updateStreakDisplay();
  }

  timer.running = false;
  timer.paused = false;
  timer.startTime = null;
  timer.elapsed = 0;
  timer.interval = null;
  updateTimerDisplay();

  document.getElementById('timerStart').disabled = false;
  document.getElementById('timerPause').disabled = true;
  document.getElementById('timerSubject').disabled = false;
  document.getElementById('timerStatus').textContent = wasRunning && savedDuration >= 1 ? `Saved ${savedDuration} min session!` : 'Ready';
}

function calculateStreak() {
  const dates = [...new Set(state.sessions.map(s => s.date))].sort();
  if (dates.length === 0) return 0;

  let streak = 0;
  const today = getToday();
  const todayDate = new Date(today + 'T00:00:00');

  for (let i = 0; i < 365; i++) {
    const d = new Date(todayDate);
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    if (dates.includes(dateStr)) {
      streak++;
    } else if (i > 0 || !dates.includes(today)) {
      if (dateStr !== today) break;
    }
  }
  return streak;
}

function logTest() {
  const subject = document.getElementById('testSubject').value;
  const score = parseInt(document.getElementById('testScore').value);
  const total = parseInt(document.getElementById('testTotal').value);

  if (isNaN(score) || isNaN(total) || total <= 0) {
    alert('Enter valid score and total.');
    return;
  }
  if (score < 0 || score > total) {
    alert(`Score must be between 0 and ${total}.`);
    return;
  }

  state.tests.push({
    subject,
    score,
    total,
    date: getToday(),
    timestamp: Date.now(),
  });
  saveState();
  document.getElementById('testScore').value = '';
  renderTestList();
  updateTestStats();
}

function deleteTest(index) {
  state.tests.splice(index, 1);
  saveState();
  renderTestList();
  updateTestStats();
}

function renderTestList() {
  const filter = document.getElementById('testFilter').value;
  const container = document.getElementById('testList');
  let tests = state.tests;

  if (filter !== 'all') {
    tests = tests.filter(t => t.subject === filter);
  }

  if (tests.length === 0) {
    container.innerHTML = '<div class="empty-state">No test scores logged yet.</div>';
    return;
  }

  container.innerHTML = [...tests].reverse().map((t, i) => {
    const idx = state.tests.length - 1 - i;
    const pct = Math.round((t.score / t.total) * 100);
    const cls = pct >= 60 ? 'highlight' : 'low';
    return `
      <div class="test-item">
        <div>
          <div class="test-item-subject">${t.subject}</div>
          <div class="test-item-date">${t.date}</div>
        </div>
        <div style="display:flex;align-items:center;gap:12px;">
          <div class="test-item-score">
            <span class="${cls}">${t.score}</span> / ${t.total}
            <span style="color:var(--text2);font-size:12px;">(${pct}%)</span>
          </div>
          <button class="test-item-delete" data-index="${idx}">🗑️</button>
        </div>
      </div>
    `;
  }).join('');

  container.querySelectorAll('.test-item-delete').forEach(btn => {
    btn.addEventListener('click', () => deleteTest(parseInt(btn.dataset.index)));
  });
}

function updateTestStats() {
  const count = state.tests.length;
  document.getElementById('testCount').textContent = count;

  if (count === 0) {
    document.getElementById('testAverage').textContent = '--';
    document.getElementById('testBest').textContent = '--';
    return;
  }

  const pcts = state.tests.map(t => (t.score / t.total) * 100);
  const avg = Math.round(pcts.reduce((a, b) => a + b, 0) / count);
  const best = Math.round(Math.max(...pcts));
  document.getElementById('testAverage').textContent = `${avg}%`;
  document.getElementById('testBest').textContent = `${best}%`;
}

function updateDashboard() {
  const streak = calculateStreak();
  document.getElementById('dashStreak').textContent = streak;

  const todayMins = getMinutesToday();
  const h = Math.floor(todayMins / 60);
  const m = todayMins % 60;
  document.getElementById('dashTodayTime').textContent = h > 0 ? `${h}h ${m}m` : `${m}m`;

  const goal = state.dailyGoal;
  const focus = goal > 0 ? Math.min(100, Math.round((todayMins / goal) * 100)) : 0;
  document.getElementById('dashFocus').textContent = `${focus}%`;

  const todaySubjects = [...new Set(getTodaySessions().map(s => s.subject))].length;
  document.getElementById('dashSubjects').textContent = state.subjects.length;
}

function renderChart(period) {
  const container = document.getElementById('progressChart');
  container.innerHTML = '';

  const today = new Date(getToday() + 'T00:00:00');
  let days = period === 'week' ? 7 : 30;
  let data = [];

  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    const mins = state.sessions
      .filter(s => s.date === dateStr)
      .reduce((sum, s) => sum + s.duration, 0);
    const label = period === 'week'
      ? d.toLocaleDateString('en', { weekday: 'short' })
      : `${d.getDate()}/${d.getMonth() + 1}`;
    data.push({ label, mins, date: dateStr });
  }

  const maxMins = Math.max(...data.map(d => d.mins), 1);

  data.forEach(d => {
    const height = Math.max(4, (d.mins / maxMins) * 140);
    const wrapper = document.createElement('div');
    wrapper.className = 'chart-bar-wrapper';
    wrapper.innerHTML = `
      <div class="chart-bar-value">${d.mins > 0 ? (d.mins >= 60 ? `${Math.floor(d.mins / 60)}h` : `${d.mins}m`) : ''}</div>
      <div class="chart-bar" style="height:${height}px;"></div>
      <div class="chart-bar-label">${d.label}</div>
    `;
    container.appendChild(wrapper);
  });
}

function renderBreakdown() {
  const container = document.getElementById('subjectBreakdown');
  const todayMins = getMinutesToday();
  const bySubject = getMinutesBySubject(getToday());

  const entries = Object.entries(bySubject).sort((a, b) => b[1] - a[1]);

  if (entries.length === 0) {
    container.innerHTML = '<div class="empty-state">No study sessions today.</div>';
    return;
  }

  container.innerHTML = entries.map(([subject, mins]) => {
    const pct = todayMins > 0 ? (mins / todayMins) * 100 : 0;
    return `
      <div class="breakdown-item">
        <span class="breakdown-subject">${subject}</span>
        <div class="breakdown-bar-bg">
          <div class="breakdown-bar" style="width:${pct}%"></div>
        </div>
        <span class="breakdown-time">${mins}m</span>
      </div>
    `;
  }).join('');
}

function exportData() {
  const blob = new Blob([JSON.stringify(state, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `focusx-backup-${getToday()}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

function clearData() {
  if (!confirm('Are you sure you want to delete all data? This cannot be undone.')) return;
  state.sessions = [];
  state.tests = [];
  saveState();
  updateDashboard();
  updateTestStats();
  renderTestList();
  renderBreakdown();
  updateTodayStats();
  updateStreakDisplay();
  renderChart('week');
}

function bindEvents() {
  // Theme toggle
  document.getElementById('themeToggle').addEventListener('click', () => {
    state.darkMode = !state.darkMode;
    applyTheme();
    saveState();
  });

  // Timer
  document.getElementById('timerStart').addEventListener('click', startTimer);
  document.getElementById('timerPause').addEventListener('click', pauseTimer);
  document.getElementById('timerReset').addEventListener('click', resetTimer);

  // Subject selection during timer
  document.getElementById('timerSubject').addEventListener('change', (e) => {
    if (!timer.running) timer.currentSubject = e.target.value;
  });

  // Tests
  document.getElementById('testAdd').addEventListener('click', logTest);
  document.getElementById('testScore').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') logTest();
  });
  document.getElementById('testTotal').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') logTest();
  });
  document.getElementById('testFilter').addEventListener('change', renderTestList);

  // Tabs
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(`tab-${tab.dataset.tab}`).classList.add('active');
      if (tab.dataset.tab === 'dashboard') {
        updateDashboard();
        renderChart(document.querySelector('.chart-btn.active')?.dataset.period || 'week');
        renderBreakdown();
        updateTodayStats();
        updateStreakDisplay();
      }
    });
  });

  // Chart period toggle
  document.querySelectorAll('.chart-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.chart-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderChart(btn.dataset.period);
    });
  });

  // Settings - Daily goal
  document.getElementById('saveGoal').addEventListener('click', () => {
    const val = parseInt(document.getElementById('dailyGoalInput').value);
    if (val && val > 0) {
      state.dailyGoal = val;
      saveState();
      updateTodayStats();
      updateDashboard();
      alert('Daily goal saved!');
    }
  });

  // Settings - Add subject
  document.getElementById('addSubject').addEventListener('click', () => {
    const input = document.getElementById('newSubjectInput');
    const name = input.value.trim();
    if (!name) return;
    if (state.subjects.includes(name)) {
      alert('Subject already exists.');
      return;
    }
    state.subjects.push(name);
    saveState();
    populateSubjectSelects();
    renderSubjectTags();
    input.value = '';
  });

  document.getElementById('newSubjectInput').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') document.getElementById('addSubject').click();
  });

  // Settings - Remove subject
  document.getElementById('subjectList').addEventListener('click', (e) => {
    if (e.target.classList.contains('remove')) {
      const subject = e.target.dataset.subject;
      if (state.subjects.length <= 1) {
        alert('Need at least one subject.');
        return;
      }
      if (!confirm(`Remove "${subject}"?`)) return;
      state.subjects = state.subjects.filter(s => s !== subject);
      saveState();
      populateSubjectSelects();
      renderSubjectTags();
    }
  });

  // Settings - Daily goal input default
  document.getElementById('dailyGoalInput').value = state.dailyGoal;

  // Settings - Export / Clear
  document.getElementById('exportData').addEventListener('click', exportData);
  document.getElementById('clearData').addEventListener('click', clearData);
}

// Handle page unload - auto-save running timer
window.addEventListener('beforeunload', () => {
  if (timer.running && !timer.paused) {
    timer.elapsed = Date.now() - timer.startTime;
    const duration = Math.round(timer.elapsed / 60000);
    if (duration >= 1) {
      state.sessions.push({
        subject: timer.currentSubject,
        date: getToday(),
        duration: duration,
        timestamp: Date.now(),
      });
      saveState();
    }
  }
});

document.addEventListener('DOMContentLoaded', init);
