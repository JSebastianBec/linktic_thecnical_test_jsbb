import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import { makeFormStub } from '../../../test/vitest.setup'
import UpdateStockPage from '../UpdateStockPage.vue'
import { useInventoryStore } from '../../stores/useInventoryStore'

function mountPage(formValid = true) {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const wrapper = mount(UpdateStockPage, {
    global: {
      plugins: [pinia],
      stubs: { QForm: makeFormStub(formValid) },
    },
  })
  return { wrapper, store: useInventoryStore() }
}

describe('UpdateStockPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('happy path: submit llama a updateStock y muestra el resultado', async () => {
    const { wrapper, store } = mountPage()
    store.updateStock.mockResolvedValue({
      id: '1', productId: 1, stock: 75,
      product: { name: 'Laptop', price: 1500 },
    })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(store.updateStock).toHaveBeenCalled()
    expect(wrapper.html()).toContain('Stock actualizado')
  })

  it('edge case: cuando updateStock devuelve null, no se muestra el panel de resultado', async () => {
    const { wrapper, store } = mountPage()
    store.updateStock.mockResolvedValue(null)

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(wrapper.html()).not.toContain('Stock actualizado')
  })

  it('edge case: muestra banner de error cuando store.error está definido', async () => {
    const { wrapper, store } = mountPage()
    store.error = 'Producto no encontrado'
    await nextTick()

    expect(wrapper.html()).toContain('Producto no encontrado')
  })

  it('edge case: validación fallida no llama a updateStock', async () => {
    const { wrapper, store } = mountPage(false)

    await wrapper.find('form').trigger('submit')
    await nextTick()

    expect(store.updateStock).not.toHaveBeenCalled()
  })
})
