import { fetchWithAuth } from './api.js';

export function initializePetitions(user) {
    const form = document.getElementById('create-petition-form');
    const list = document.getElementById('petition-list');

    async function load() {
        list.innerHTML = 'Завантаження…';
        try {
            const resp = await fetchWithAuth(
                `/api/petitions/user/${user.id}?schoolId=${user.schoolId}&classId=${user.classId || ''}`
            );
            if (!resp.ok) throw await resp.text();
            const petitions = await resp.json();
            renderList(petitions);
        } catch (err) {
            list.innerHTML = `<p class="error">Не вдалося завантажити петиції: ${err}</p>`;
        }
    }

    function renderList(petitions) {
        if (!petitions.length) {
            list.innerHTML = '<p>Немає активних петицій.</p>';
            return;
        }
        list.innerHTML = petitions.map(p => {
            const pct = Math.min(100, Math.round((p.currentVotes / p.threshold) * 100));
            const isOpen = new Date(p.endDate) > new Date();
            return `
      <div class="petition-card">
        <h4>${p.title}</h4>
        <div class="petition-meta">
          <span>Дедлайн: ${new Date(p.endDate).toLocaleDateString()}</span> |
          <span>Підписали: ${p.currentVotes} / ${p.threshold}</span>
        </div>
        <div class="petition-progress">
          <div class="petition-progress-bar" style="width:${pct}%;"></div>
        </div>
        <div>${p.description}</div>
        <div class="petition-actions">
          ${!p.approvedByDirector && isOpen
                    ? `
            <button class="yes" onclick="votePetition(${p.id}, 'YES')">Підтримати</button>
            <button class="no"  onclick="votePetition(${p.id}, 'NO')">Відхилити</button>
            `
                    : p.approvedByDirector
                        ? `<button class="disabled">✅ Ухвалено директором</button>`
                        : `<button class="disabled">❌ Закрито</button>`
                }
        </div>
      </div>`;
        }).join('');
    }

    window.votePetition = async (id, variant) => {
        try {
            const resp = await fetchWithAuth(`/api/petitions/${id}/vote`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ vote: variant })
            });

            if (!resp.ok) {
                const errText = await resp.text();
                throw new Error(errText || resp.statusText);
            }
            await load();
        } catch (err) {
            alert('Не вдалося проголосувати: ' + err.message);
        }
    };


    form.addEventListener('submit', async e => {
        e.preventDefault();
        const title = form.querySelector('#petition-title').value.trim();
        const description = form.querySelector('#petition-description').value.trim();
        const level = form.querySelector('#petition-target').value;
        const endDate = form.querySelector('#petition-deadline').value;
        if (!title || !description || !endDate) {
            return alert('Будь ласка, заповніть усі поля.');
        }

        const payload = {
            title,
            description,
            startDate: new Date().toISOString().split('T')[0],
            endDate: new Date(endDate).toISOString().split('T')[0],
            classId: level === 'CLASS' ? user.classId : null
        };
        const rawDate = form.querySelector('#petition-deadline').value;
        payload.startDate = rawDate + 'T00:00:00';
        payload.endDate = rawDate + 'T23:59:59';


        try {
            const resp = await fetchWithAuth('/api/createPetition', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!resp.ok) {
                const err = await resp.text();
                throw err || resp.statusText;
            }
            form.reset();
            await load();
        } catch (err) {
            alert('Помилка створення петиції: ' + err);
        }
    });

    load();
}
