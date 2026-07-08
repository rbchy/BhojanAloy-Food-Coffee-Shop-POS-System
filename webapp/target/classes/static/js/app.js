/* Shared fetch helpers for the Bhojan-Aloy web demo. */

function apiRequest(method, url, body) {
    return fetch(url, {
        method: method,
        credentials: 'include',
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined
    }).then(function (res) {
        return res.json().catch(function () { return {}; }).then(function (data) {
            if (!res.ok) {
                var err = new Error(data.message || ('Request failed with status ' + res.status));
                err.status = res.status;
                throw err;
            }
            return data;
        });
    });
}

function apiGet(url) { return apiRequest('GET', url); }
function apiPost(url, body) { return apiRequest('POST', url, body); }

/** Redirects to login.html if the current session isn't authenticated. Call at the top of protected pages. */
function requireLogin() {
    return apiGet('/api/auth/me').catch(function () {
        window.location.href = 'login.html';
        throw new Error('redirecting to login');
    });
}

function logout() {
    apiPost('/api/auth/logout').finally(function () {
        window.location.href = 'login.html';
    });
}
