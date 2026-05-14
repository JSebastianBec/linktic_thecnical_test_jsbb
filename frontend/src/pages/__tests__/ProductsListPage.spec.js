import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import ProductsListPage from '../ProductsListPage.vue'
import { useProductStore } from '../../stores/useProductStore'

function mountPage(initialState = {}) {
  const pinia = createTestingPinia({ createSpy: vi.fn, initialState: { products: initialState } })
  const wrapper = mount(ProductsListPage, { global: { plugins: [pinia] } })
  return { wrapper, store: useProductStore() }
}

describe('ProductsListPage', () => {
  it('happy path: llama a fetchAll al montar el componente', () => {
    const { store } = mountPage()
    expect(store.fetchAll).toHaveBeenCalledOnce()
  })

  it('happy path: calcula avgPrice y maxPrice correctamente con varios productos', async () => {
    const { wrapper, store } = mountPage()
    store.products = [
      { id: 1, name: 'A', price: 100 },
      { id: 2, name: 'B', price: 300 },
    ]
    await nextTick()

    expect(wrapper.html()).toContain('200.00') // avgPrice
    expect(wrapper.html()).toContain('300.00') // maxPrice
  })

  it('edge case: lista vacía muestra avgPrice y maxPrice en 0.00', async () => {
    const { wrapper, store } = mountPage()
    store.products = []
    await nextTick()

    const html = wrapper.html()
    expect(html).toContain('0.00')
  })

  it('edge case: muestra el banner de error cuando store.error está definido', async () => {
    const { wrapper, store } = mountPage()
    store.error = 'Error al cargar productos'
    await nextTick()

    expect(wrapper.html()).toContain('Error al cargar productos')
  })
})
