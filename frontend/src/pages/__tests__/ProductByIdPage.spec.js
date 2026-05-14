import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import { makeFormStub } from '../../../test/vitest.setup'
import ProductByIdPage from '../ProductByIdPage.vue'
import { useProductStore } from '../../stores/useProductStore'

function mountPage(formValid = true) {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const wrapper = mount(ProductByIdPage, {
    global: {
      plugins: [pinia],
      stubs: { QForm: makeFormStub(formValid) },
    },
  })
  return { wrapper, store: useProductStore() }
}

describe('ProductByIdPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('happy path: search llama a store.fetchById y muestra el resultado', async () => {
    const { wrapper, store } = mountPage()
    store.fetchById.mockResolvedValue({ id: '5', name: 'Monitor', price: 800, description: 'Full HD' })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(store.fetchById).toHaveBeenCalled()
    expect(wrapper.html()).toContain('Monitor')
  })

  it('edge case: cuando fetchById devuelve null, no se muestra tarjeta de producto', async () => {
    const { wrapper, store } = mountPage()
    store.fetchById.mockResolvedValue(null)

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(wrapper.html()).not.toContain('Detalles del producto')
  })

  it('edge case: muestra banner de error cuando store.error está definido', async () => {
    const { wrapper, store } = mountPage()
    store.error = 'Producto no encontrado'
    await nextTick()

    expect(wrapper.html()).toContain('Producto no encontrado')
  })
})
