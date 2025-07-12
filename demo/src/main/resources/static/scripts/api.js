export async function fetchWithAuth(url, opts = {}) {
  opts.credentials = 'include';
  opts.headers = {
    'Content-Type': 'application/json',
    ...(opts.headers || {})
  };
  return fetch(url, opts);
}
export async function fetchWithAuthAndBody(url, body, opts = {}) {
  opts.method = 'POST';
  opts.body = JSON.stringify(body);
  return fetchWithAuth(url, opts);
}
