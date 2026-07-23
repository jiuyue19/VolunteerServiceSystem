import request from '@/utils/request'

// 同步志愿者总时长到区块链
export const syncHours = (data) => {
  return request.post('/blockchain/sync-hours', data)
}

// 补录时长上链
export const replenishHours = (data) => {
  return request.post('/blockchain/replenish-hours', data)
}

// 活动记录上链
export const activityRecord = (data) => {
  return request.post('/blockchain/activity-record', data)
}

// 查询链上总时长
export const getTotalHours = (volunteerId) => {
  return request.get(`/blockchain/total-hours/${volunteerId}`)
}

// 查询链上记录数量
export const getRecordCount = (volunteerId) => {
  return request.get(`/blockchain/record-count/${volunteerId}`)
}

// 绑定钱包地址
export const bindWallet = (data) => {
  return request.post('/blockchain/bind-wallet', data)
}

// 获取志愿者信息（包含钱包地址）
export const getVolunteerInfo = (volunteerId) => {
  return request.get(`/volunteer/${volunteerId}`)
}

// 获取区块链统计数据
export const getBlockchainStats = () => {
  return request.get('/blockchain/stats')
}

// 获取合约配置信息
export const getContractInfo = () => {
  return request.get('/blockchain/contract-info')
}

// 查询志愿者上链记录详情
export const getChainRecords = (volunteerId) => {
  return request.get(`/blockchain/chain-records/${volunteerId}`)
}

// 查询所有上链记录列表
export const getAllChainRecords = () => {
  return request.get('/blockchain/all-chain-records')
}

// 根据志愿者ID查询钱包地址
export const getWalletAddress = (volunteerId) => {
  return request.get(`/blockchain/wallet-address/${volunteerId}`)
}

// 根据钱包地址查询链上数据
export const getChainDataByWallet = (walletAddress) => {
  return request.get('/blockchain/chain-data-by-wallet', {
    params: { walletAddress }
  })
}