const state = {
  // 服务人员列表
  housekeeperList: [],
  // 当前选中的服务人员
  selectedHousekeeper: null,
  // 筛选条件
  filterConditions: {
    gender: '',
    origin: '',
    minRating: 0,
    minYears: 0,
    serviceTypeId: null
  },
  // 服务类型列表
  serviceTypes: [],
  // 可用服务列表
  availableServices: [],
  // 购物车状态（临时预约信息）
  cart: {
    serviceId: null,
    staffId: null,
    appointmentDate: null,
    appointmentTime: null,
    notes: ''
  }
}

const getters = {
  housekeeperList: state => state.housekeeperList,
  selectedHousekeeper: state => state.selectedHousekeeper,
  filterConditions: state => state.filterConditions,
  serviceTypes: state => state.serviceTypes,
  availableServices: state => state.availableServices,
  cart: state => state.cart
}

const mutations = {
  SET_HOUSEKEEPER_LIST(state, list) {
    state.housekeeperList = list
  },
  SET_SELECTED_HOUSEKEEPER(state, housekeeper) {
    state.selectedHousekeeper = housekeeper
  },
  SET_FILTER_CONDITIONS(state, conditions) {
    state.filterConditions = { ...state.filterConditions, ...conditions }
  },
  RESET_FILTER_CONDITIONS(state) {
    state.filterConditions = {
      gender: '',
      origin: '',
      minRating: 0,
      minYears: 0,
      serviceTypeId: null
    }
  },
  SET_SERVICE_TYPES(state, types) {
    state.serviceTypes = types
  },
  SET_AVAILABLE_SERVICES(state, services) {
    state.availableServices = services
  },
  SET_CART(state, cart) {
    state.cart = { ...state.cart, ...cart }
  },
  CLEAR_CART(state) {
    state.cart = {
      serviceId: null,
      staffId: null,
      appointmentDate: null,
      appointmentTime: null,
      notes: ''
    }
  }
}

const actions = {
  setHousekeeperList({ commit }, list) {
    commit('SET_HOUSEKEEPER_LIST', list)
  },
  setSelectedHousekeeper({ commit }, housekeeper) {
    commit('SET_SELECTED_HOUSEKEEPER', housekeeper)
  },
  updateFilterConditions({ commit }, conditions) {
    commit('SET_FILTER_CONDITIONS', conditions)
  },
  resetFilterConditions({ commit }) {
    commit('RESET_FILTER_CONDITIONS')
  },
  setServiceTypes({ commit }, types) {
    commit('SET_SERVICE_TYPES', types)
  },
  setAvailableServices({ commit }, services) {
    commit('SET_AVAILABLE_SERVICES', services)
  },
  updateCart({ commit }, cart) {
    commit('SET_CART', cart)
  },
  clearCart({ commit }) {
    commit('CLEAR_CART')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}

