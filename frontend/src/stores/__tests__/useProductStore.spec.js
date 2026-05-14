import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProductStore } from '../useProductStore'
import { productApi } from '../../services/api'

vi.mock('../../services/api', () => ({
  productApi: { get: vi.fn(), post: vi.fn() },
  extractApiError: vi.fn((err) => err?.response?.data?.errors?.[0]?.detail ?? 'Error'),
}))

const PRODUCT = { id: '1', name: 'Laptop', price: 1500, description: 'Gaming laptop' }

const toApiItem = (p) => ({
  data: { data: { id: String(p.id), attributes: { ...p } } },
})
const toApiList = (items) => ({
  data: { data: items.map((p) => ({ id: String(p.id), attributes: { ...p } })) },
})

describe('useProductStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // ── fetchAll ──────────────────────────────────────────────────────────────

  it('fetchAll — happy path: maps list and clears loading/error', async () => {
    productApi.get.mockResolvedValue(toApiList([PRODUCT]))
    const store = useProductStore()

    await store.fetchAll()

    expect(store.products).toHaveLength(1)
    expect(store.products[0]).toMatchObject({ name: 'Laptop', price: 1500 })
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetchAll — edge case: sets error and keeps empty list on API failure', async () => {
    productApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'Server error' }] } } })
    const store = useProductStore()

    await store.fetchAll()

    expect(store.products).toHaveLength(0)
    expect(store.error).toBe('Server error')
    expect(store.loading).toBe(false)
  })

  it('fetchAll — edge case: loading is true during fetch, false after', async () => {
    let resolve
    productApi.get.mockReturnValue(new Promise((r) => { resolve = r }))
    const store = useProductStore()

    const promise = store.fetchAll()
    expect(store.loading).toBe(true)

    resolve(toApiList([]))
    await promise
    expect(store.loading).toBe(false)
  })

  // ── fetchById ─────────────────────────────────────────────────────────────

  it('fetchById — happy path: returns product with correct fields', async () => {
    productApi.get.mockResolvedValue(toApiItem(PRODUCT))
    const store = useProductStore()

    const result = await store.fetchById(1)

    expect(result).toMatchObject({ name: 'Laptop', price: 1500 })
    expect(productApi.get).toHaveBeenCalledWith('/1')
  })

  it('fetchById — edge case: returns null and sets error when not found', async () => {
    productApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'Not found' }] } } })
    const store = useProductStore()

    const result = await store.fetchById(999)

    expect(result).toBeNull()
    expect(store.error).toBe('Not found')
  })

  // ── create ────────────────────────────────────────────────────────────────

  it('create — happy path: posts payload and returns created product', async () => {
    productApi.post.mockResolvedValue(toApiItem(PRODUCT))
    const store = useProductStore()

    const result = await store.create({ name: 'Laptop', price: 1500 })

    expect(result).toMatchObject({ name: 'Laptop' })
    expect(productApi.post).toHaveBeenCalledWith('', { name: 'Laptop', price: 1500 })
  })

  it('create — edge case: returns null and sets error on validation failure', async () => {
    productApi.post.mockRejectedValue({ response: { data: { errors: [{ detail: 'Nombre inválido' }] } } })
    const store = useProductStore()

    const result = await store.create({ name: '123', price: -1 })

    expect(result).toBeNull()
    expect(store.error).toBe('Nombre inválido')
  })

  // ── clearError ────────────────────────────────────────────────────────────

  it('clearError — resets error to null', async () => {
    productApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'Algo falló' }] } } })
    const store = useProductStore()
    await store.fetchAll()
    expect(store.error).toBeTruthy()

    store.clearError()

    expect(store.error).toBeNull()
  })
})
