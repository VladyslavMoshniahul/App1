import { fetchWithAuth } from './api.js';

export async function renderVoteCreation(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
    <section id="vote-create-section">
      <h2>Створити голосування</h2>
      <form id="vote-create-form">
        <input name="title"        placeholder="Назва голосування" required><br>
        <textarea name="description" placeholder="Опис"></textarea><br>
        <label>Рівень:
          <select name="level" id="vote-level">
            <option value="SCHOOL">Вся школа</option>
            <option value="CLASS">Клас</option>
            <option value="TEACHERS_GROUP">Група вчителів</option>
            <option value="SELECTED">Вибрані учасники</option>
          </select>
        </label><br>
        <div id="vote-level-extra"></div>
        <label>Початок: <input type="datetime-local" name="startDate" required></label><br>
        <label>Дедлайн: <input type="datetime-local" name="endDate"   required></label><br>
        <label><input type="checkbox" name="multipleChoice"> Дозволити декілька відповідей</label><br>
        <div id="vote-options-list">
          <input class="vote-option" name="option" placeholder="Варіант 1" required>
          <input class="vote-option" name="option" placeholder="Варіант 2" required>
        </div>
        <button type="button" id="add-vote-option">Додати варіант</button><br>
        <button type="submit">Створити голосування</button>
      </form>
      <div id="vote-create-result"></div>
    </section>
  `;

    document.getElementById('add-vote-option').onclick = () => {
        const list = document.getElementById('vote-options-list');
        const input = document.createElement('input');
        input.className = 'vote-option';
        input.name = 'option';
        input.placeholder = 'Варіант';
        list.appendChild(input);
    };

    async function loadParticipants(extra, schoolId, classId, userType) {
        const container = extra.querySelector('#user-checkboxes');
        container.innerHTML = 'Завантаження...';
        let url = `/api/${userType === 'teachers' ? 'teachers' : 'users'}?schoolId=${schoolId}`;
        if (classId) url += `&classId=${classId}`;
        try {
            const resp = await fetchWithAuth(url);
            let list = await resp.json();

            if (classId) {
                list = list.filter(u => u.classId === +classId);
            }

            const seen = new Set();
            list = list.filter(u => {
                if (seen.has(u.id)) return false;
                seen.add(u.id);
                return true;
            });

            if (!list.length) {
                container.innerHTML = '<div>Немає користувачів для вибору.</div>';
                return;
            }
            container.innerHTML = list.map(u =>
                `<label style="display:block;">
          <input type="checkbox" class="participant-checkbox" value="${u.id}">
          ${u.firstName} ${u.lastName} (${u.email})
        </label>`
            ).join('');
        } catch {
            container.innerHTML = '<div>Не вдалося завантажити користувачів.</div>';
        }
    }

    document.getElementById('vote-level').onchange = async (e) => {
        const extra = document.getElementById('vote-level-extra');
        const val = e.target.value;
        let user;
        try {
            const resp = await fetchWithAuth('/api/me');
            user = await resp.json();
        } catch {
            extra.innerHTML = '<div>Не вдалося отримати дані користувача.</div>';
            return;
        }

        if (val === 'CLASS') {
            extra.innerHTML = '';
            await renderClassDropdown(extra, user.schoolId, user.classId);
            return;
        }

        if (val === 'TEACHERS_GROUP' || val === 'SELECTED') {
            const classesResp = await fetchWithAuth(`/api/classes?schoolId=${user.schoolId}`);
            const classes = await classesResp.json();

            extra.innerHTML = `
        <label>Клас:
          <select id="vote-class-select">
            <option value="">Всі класи</option>
            ${classes.map(c =>
                `<option value="${c.id}" ${c.id === user.classId ? 'selected' : ''}>${c.name}</option>`
            ).join('')}
          </select>
        </label>
        <div id="user-checkboxes" style="margin-top:8px;"></div>
      `;
            const sel = document.getElementById('vote-class-select');
            const reload = () => loadParticipants(extra, user.schoolId, sel.value, val === 'TEACHERS_GROUP' ? 'teachers' : 'users');
            sel.addEventListener('change', reload);
            await reload();
            return;
        }

        extra.innerHTML = '';
    };

    document.getElementById('vote-level').dispatchEvent(new Event('change'));

    document.getElementById('vote-create-form').onsubmit = async (e) => {
        e.preventDefault();
        const form = e.target;
        let user;
        try {
            const resp = await fetchWithAuth('/api/me');
            user = await resp.json();
        } catch {
            document.getElementById('vote-create-result').innerText = 'Не вдалося отримати дані користувача.';
            return;
        }
        const levelMap = {
            SCHOOL: 'SCHOOL',
            CLASS: 'ACLASS',
            TEACHERS_GROUP: 'TEACHERS_GROUP',
            SELECTED: 'SELECTED_USERS'
        };
        const data = {
            schoolId: user.schoolId,
            classId: null,
            title: form.title.value,
            description: form.description.value,
            startDate: new Date(form.startDate.value).toISOString(),
            endDate: new Date(form.endDate.value).toISOString(),
            createdBy: user.id,
            multipleChoice: form.multipleChoice.checked,
            votingLevel: levelMap[form.level.value],
            status: 'OPEN',
            variants: Array.from(form.querySelectorAll('.vote-option'))
                .map(i => ({ text: i.value }))
                .filter(v => v.text.trim())
        };
        if (data.votingLevel === 'ACLASS') {
            data.classId = form.querySelector('#vote-class-select')?.value || user.classId;
        }
        if ((data.votingLevel === 'TEACHERS_GROUP' || data.votingLevel === 'SELECTED_USERS')) {
            const checked = Array.from(
                document.querySelectorAll('#vote-level-extra .participant-checkbox:checked')
            ).map(cb => Number(cb.value));
            if (!checked.length) {
                document.getElementById('vote-create-result').innerText = 'Оберіть хоча б одного учасника.';
                return;
            }
            data.participants = checked.map(id => ({ userId: id }));
        }
        if (data.variants.length < 2) {
            document.getElementById('vote-create-result').innerText = 'Додайте хоча б два варіанти.';
            return;
        }
        try {
            const resp = await fetchWithAuth('/api/createVoting', {
                method: 'POST',
                body: JSON.stringify(data)
            });
            if (resp.ok) {
                document.getElementById('vote-create-result').innerText = 'Голосування створено!';
                form.reset();
                document.getElementById('vote-level').dispatchEvent(new Event('change'));
            } else {
                const err = await resp.text();
                document.getElementById('vote-create-result').innerText = 'Помилка: ' + err;
            }
        } catch {
            document.getElementById('vote-create-result').innerText = 'Помилка з’єднання.';
        }
    };
}

export async function renderClassDropdown(extraDiv, schoolId, userClassId) {
    extraDiv.innerHTML = 'Завантаження класів...';
    try {
        const resp = await fetchWithAuth(`/api/classes?schoolId=${schoolId}`);
        const classes = await resp.json();
        if (!classes.length) {
            extraDiv.innerHTML = '<div>Немає класів для вибору.</div>';
            return;
        }
        extraDiv.innerHTML = `
      <label>Клас:
        <select id="vote-class-select">
          <option value="">Всі класи</option>
          ${classes.map(c =>
            `<option value="${c.id}" ${c.id === userClassId ? 'selected' : ''}>${c.name}</option>`
        ).join('')}
        </select>
      </label>
    `;
    } catch {
        extraDiv.innerHTML = '<div>Не вдалося завантажити класи.</div>';
    }
}

export async function renderAvailableVotes(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '<div>Завантаження голосувань…</div>';

    let user;
    try {
        const me = await fetchWithAuth('/api/me');
        user = await me.json();
    } catch {
        container.innerHTML = '<div>Будь ласка, увійдіть.</div>';
        return;
    }

    let votes;
    try {
        const url = `/api/votes?schoolId=${user.schoolId}`;
        const resp = await fetchWithAuth(url);
        votes = await resp.json();
    } catch {
        container.innerHTML = '<div>Не вдалося завантажити голосування.</div>';
        return;
    }

    votes = votes.filter(vote => {
        switch (vote.votingLevel) {
            case 'SCHOOL':
                return true;
            case 'ACLASS':
                return vote.classId === user.classId;
            case 'TEACHERS_GROUP':
            case 'SELECTED_USERS':
                return vote.participants.some(p => p.userId === user.id);
            default:
                return false;
        }
    });

    if (!votes.length) {
        container.innerHTML = '<div>Немає доступних голосувань.</div>';
        return;
    }

    container.innerHTML = votes.map(vote => `
      <section class="vote-section">
        <h3>${vote.title}</h3>
        <p>${vote.description || ''}</p>
        <form class="vote-form" data-vote-id="${vote.id}">
          ${vote.variants.map(variant => `
            <label>
              <input type="${vote.multipleChoice ? 'checkbox' : 'radio'}"
                     name="variant" value="${variant.id}">
              ${variant.text}
            </label><br>`).join('')}
          <button type="submit">Проголосувати</button>
          <span class="vote-result"></span>
        </form>
      </section>
    `).join('');

    container.querySelectorAll('.vote-form').forEach(form => {
        form.addEventListener('submit', async e => {
            e.preventDefault();
            const voteId = form.dataset.voteId;
            const selectedIds = Array.from(
                form.querySelectorAll('input[name="variant"]:checked')
            ).map(i => Number(i.value));

            if (!selectedIds.length) {
                form.querySelector('.vote-result').textContent = 'Оберіть варіант!';
                return;
            }

            try {
                const resp = await fetchWithAuth(`/api/voting/${voteId}/vote`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(selectedIds)
                });

                if (resp.ok) {
                    form.querySelector('.vote-result').textContent = 'Голос зараховано!';
                    form.querySelector('button').disabled = true;
                } else {
                    const err = await resp.text();
                    form.querySelector('.vote-result').textContent = 'Помилка: ' + err;
                }
            } catch {
                form.querySelector('.vote-result').textContent = 'Помилка з’єднання.';
            }
        });
    });
}
