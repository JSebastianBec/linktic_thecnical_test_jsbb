import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi, extractApiError } from '../services/api'

export const useProductStore = defineStore('products', () => {
  const products = ref([])
  const loading = ref(false)
  const error = ref(null)

  const clearError = () => (error.value = null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const res = await productApi.get('/')
      products.value = res.data.data.map((item) => ({
        id: item.id,
        ...item.attributes,
      }))
    } catch (err) {
      error.value = extractApiError(err)
    } finally {
      loading.value = false
    }
  }

  async function fetchById(id) {
    loading.value = true
    error.value = null
    try {
      const res = await productApi.get(`/${id}`)
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function create(payload) {
    loading.value = true
    error.value = null
    try {
      const res = await productApi.post('/', payload)
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  return { products, loading, error, clearError, fetchAll, fetchById, create }
})
