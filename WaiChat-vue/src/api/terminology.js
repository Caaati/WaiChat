import axios from 'axios';

const withCreds = { withCredentials: true };
const jsonCreds = {
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
};

export function fetchSystemTerms() {
  return axios.get('/api/terminology/system', withCreds);
}

/** 系统术语：按组 + 无组 */
export function fetchSystemGroups() {
  return axios.get('/api/terminology/system/groups', withCreds);
}

export function fetchMyTerms() {
  return axios.get('/api/terminology/mine', withCreds);
}

/** 我的术语：按组聚合 */
export function fetchMineGrouped() {
  return axios.get('/api/terminology/mine/grouped', withCreds);
}

export function createMyTerm(payload) {
  return axios.post('/api/terminology/mine', payload, jsonCreds);
}

export function updateMyTerm(id, payload) {
  return axios.put(`/api/terminology/mine/${id}`, payload, jsonCreds);
}

export function deleteMyTerm(id) {
  return axios.delete(`/api/terminology/mine/${id}`, withCreds);
}

/**
 * @param {object} payload - { systemTerminologyIds?: number[], systemGroupIds?: number[] }
 */
export function importSystemTermsToMine(payload) {
  const body = {
    systemTerminologyIds: payload.systemTerminologyIds || [],
    systemGroupIds: payload.systemGroupIds || []
  };
  return axios.post('/api/terminology/mine/import-system', body, jsonCreds);
}
