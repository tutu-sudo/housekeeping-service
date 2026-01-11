const state = {
  // 当前订单
  currentOrder: null,
  // 订单历史列表
  orderHistory: [],
  // 订单详情
  orderDetail: null
}

const getters = {
  currentOrder: state => state.currentOrder,
  orderHistory: state => state.orderHistory,
  orderDetail: state => state.orderDetail
}

const mutations = {
  SET_CURRENT_ORDER(state, order) {
    state.currentOrder = order
  },
  SET_ORDER_HISTORY(state, orders) {
    state.orderHistory = orders
  },
  SET_ORDER_DETAIL(state, order) {
    state.orderDetail = order
  },
  CLEAR_CURRENT_ORDER(state) {
    state.currentOrder = null
  }
}

const actions = {
  setCurrentOrder({ commit }, order) {
    commit('SET_CURRENT_ORDER', order)
  },
  setOrderHistory({ commit }, orders) {
    commit('SET_ORDER_HISTORY', orders)
  },
  setOrderDetail({ commit }, order) {
    commit('SET_ORDER_DETAIL', order)
  },
  clearCurrentOrder({ commit }) {
    commit('CLEAR_CURRENT_ORDER')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}

