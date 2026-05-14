import axios from 'axios'

const API_KEY = import.meta.env.VITE_API_KEY || 'local-dev-key'

export const productApi = axios.create({
  baseURL: '/api/products',
  headers: { 'X-API-KEY': API_KEY, 'Content-Type': 'application/json' },
})

export const inventoryApi = axios.create({
  baseURL: '/api/inventory',
  headers: { 'X-API-KEY': API_KEY, 'Content-Type': 'application/json' },
})

const extractApiError = (err) =>
  err.response?.data?.errors?.[0]?.detail || err.message || 'Unexpected error'

export { extractApiError }
