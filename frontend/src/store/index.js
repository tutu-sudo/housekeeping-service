import { createStore } from 'vuex'
import user from './modules/user'
import business from './modules/business'
import order from './modules/order'

export default createStore({
  modules: {
    user,
    business,
    order
  }
})

