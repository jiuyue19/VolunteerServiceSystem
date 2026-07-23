/**
 * 区块链配置文件
 * 用于前端与智能合约交互
 */
import { BrowserProvider, Contract, ContractFactory } from 'ethers'
import { VOLUNTEER_HOURS_SOURCE } from '@/config/volunteerHoursSource'

// VolunteerHours 合约 ABI（应用二进制接口）
export const VOLUNTEER_HOURS_ABI = [
	{
		"inputs": [],
		"stateMutability": "nonpayable",
		"type": "constructor"
	},
	{
		"anonymous": false,
		"inputs": [
			{
				"indexed": true,
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "replenishDuration",
				"type": "uint256"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			},
			{
				"indexed": false,
				"internalType": "string",
				"name": "activityName",
				"type": "string"
			}
		],
		"name": "DurationReplenished",
		"type": "event"
	},
	{
		"anonymous": false,
		"inputs": [
			{
				"indexed": true,
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "totalMinutes",
				"type": "uint256"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			}
		],
		"name": "DurationUpdated",
		"type": "event"
	},
	{
		"anonymous": false,
		"inputs": [
			{
				"indexed": true,
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "duration",
				"type": "uint256"
			},
			{
				"indexed": false,
				"internalType": "bool",
				"name": "isReplenish",
				"type": "bool"
			},
			{
				"indexed": false,
				"internalType": "string",
				"name": "activityName",
				"type": "string"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			}
		],
		"name": "RecordAdded",
		"type": "event"
	},
	{
		"inputs": [
			{
				"internalType": "string",
				"name": "activityName",
				"type": "string"
			},
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"internalType": "uint256",
				"name": "duration",
				"type": "uint256"
			}
		],
		"name": "addActivity",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "string",
				"name": "activityName",
				"type": "string"
			},
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"internalType": "uint256",
				"name": "duration",
				"type": "uint256"
			}
		],
		"name": "addReplenishActivity",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [],
		"name": "admin",
		"outputs": [
			{
				"internalType": "address",
				"name": "",
				"type": "address"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "string[]",
				"name": "activityNames",
				"type": "string[]"
			},
			{
				"internalType": "address[]",
				"name": "volunteers",
				"type": "address[]"
			},
			{
				"internalType": "uint256[]",
				"name": "durations",
				"type": "uint256[]"
			}
		],
		"name": "batchAddActivities",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "string[]",
				"name": "activityNames",
				"type": "string[]"
			},
			{
				"internalType": "address[]",
				"name": "volunteers",
				"type": "address[]"
			},
			{
				"internalType": "uint256[]",
				"name": "durations",
				"type": "uint256[]"
			}
		],
		"name": "batchAddReplenishActivities",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getAllRecords",
		"outputs": [
			{
				"components": [
					{
						"internalType": "string",
						"name": "activityName",
						"type": "string"
					},
					{
						"internalType": "uint256",
						"name": "duration",
						"type": "uint256"
					},
					{
						"internalType": "uint256",
						"name": "timestamp",
						"type": "uint256"
					},
					{
						"internalType": "bool",
						"name": "isReplenish",
						"type": "bool"
					}
				],
				"internalType": "struct VolunteerHours.ActivityRecord[]",
				"name": "",
				"type": "tuple[]"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			},
			{
				"internalType": "uint256",
				"name": "index",
				"type": "uint256"
			}
		],
		"name": "getRecord",
		"outputs": [
			{
				"internalType": "string",
				"name": "activityName",
				"type": "string"
			},
			{
				"internalType": "uint256",
				"name": "duration",
				"type": "uint256"
			},
			{
				"internalType": "uint256",
				"name": "timestamp",
				"type": "uint256"
			},
			{
				"internalType": "bool",
				"name": "isReplenish",
				"type": "bool"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getRecordCount",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getReplenishHours",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getReplenishMinutes",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getTotalHours",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "volunteer",
				"type": "address"
			}
		],
		"name": "getTotalMinutes",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "",
				"type": "address"
			}
		],
		"name": "replenishMinutes",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "",
				"type": "address"
			}
		],
		"name": "totalMinutes",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "newAdmin",
				"type": "address"
			}
		],
		"name": "transferAdmin",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	}
]

// TODO: 部署合约后，将此地址替换为实际部署的合约地址
export const CONTRACT_ADDRESS = '0xfdD7032F12212b0a38a72374259ed4509bbdbfB7'

// 网络配置
export const NETWORK_CONFIG = {
  // 私链配置
  PRIVATE: {
    chainId: '0x7e9', // 2025 的十六进制
    chainName: 'MyPrivateChain',
    rpcUrls: ['http://127.0.0.1:8545'],
    nativeCurrency: {
      name: 'Ethereum',
      symbol: 'ETH',
      decimals: 18
    },
    blockExplorerUrls: [] // 私链无浏览器
  },
  
  // Sepolia 测试网配置（可选）
  SEPOLIA: {
    chainId: '0xaa36a7',
    chainName: 'Sepolia Test Network',
    rpcUrls: ['https://sepolia.infura.io/v3/YOUR-PROJECT-ID'],
    nativeCurrency: {
      name: 'Sepolia Ether',
      symbol: 'SEP',
      decimals: 18
    },
    blockExplorerUrls: ['https://sepolia.etherscan.io']
  }
}

// 当前使用的网络（开发环境使用私链）
export const CURRENT_NETWORK = NETWORK_CONFIG.PRIVATE

const CONTRACT_STORAGE_KEY = 'volunteer-hours-contract-address'

export function getStoredContractAddress() {
  if (typeof window === 'undefined') {
    return CONTRACT_ADDRESS
  }

  const storedAddress = window.localStorage.getItem(CONTRACT_STORAGE_KEY)

  if (!storedAddress) {
    return CONTRACT_ADDRESS
  }

  const normalizedStored = storedAddress.trim().toLowerCase()
  const normalizedConfigured = (CONTRACT_ADDRESS || '').trim().toLowerCase()

  if (normalizedConfigured && normalizedStored !== normalizedConfigured) {
    return CONTRACT_ADDRESS
  }

  return storedAddress
}

export function saveContractAddress(address) {
  if (typeof window !== 'undefined' && address) {
    window.localStorage.setItem(CONTRACT_STORAGE_KEY, address)
  }
}

export function clearStoredContractAddress() {
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(CONTRACT_STORAGE_KEY)
  }
}


/**
 * 检查 MetaMask 是否已安装
 */
export function checkMetaMaskInstalled() {
  return typeof window.ethereum !== 'undefined'
}

/**
 * 连接 MetaMask
 */
export async function connectMetaMask() {
  if (!checkMetaMaskInstalled()) {
    throw new Error('请先安装 MetaMask 浏览器插件')
  }
  
  try {
    const accounts = await window.ethereum.request({ 
      method: 'eth_requestAccounts' 
    })
    return accounts[0]
  } catch (error) {
    throw new Error('连接 MetaMask 失败: ' + error.message)
  }
}

/**
 * 切换到指定网络
 */
export async function switchNetwork(networkConfig) {
  try {
    await window.ethereum.request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId: networkConfig.chainId }],
    })
  } catch (switchError) {
    // 如果网络不存在，添加网络
    if (switchError.code === 4902) {
      await window.ethereum.request({
        method: 'wallet_addEthereumChain',
        params: [networkConfig],
      })
    } else {
      throw switchError
    }
  }
}

/**
 * 获取当前网络 ID
 */
export async function getCurrentChainId() {
  const chainId = await window.ethereum.request({ method: 'eth_chainId' })
  return chainId
}

export async function getMetaMaskProvider() {
  if (!checkMetaMaskInstalled()) {
    throw new Error('请先安装 MetaMask 浏览器插件')
  }

  // BrowserProvider 是 ethers v6 中适配浏览器钱包注入对象的提供者。
  // 这里的 window.ethereum 由 MetaMask 注入，后续查询账户、签名、发交易都依赖它。
  return new BrowserProvider(window.ethereum)
}

export async function getMetaMaskSigner() {
  // 获取 signer 前，先确保两个前置条件成立：
  // 1. 用户已经授权当前页面访问钱包账户
  // 2. 钱包已经切换到系统要求的目标链（这里是本地私链）
  await connectMetaMask()
  await switchNetwork(CURRENT_NETWORK)
  const provider = await getMetaMaskProvider()

  // signer 代表“当前用于签名交易的钱包账户”。
  // 只读调用可以只用 provider，但写链操作必须使用 signer。
  return provider.getSigner()
}

export async function getMetaMaskAddress() {
  const signer = await getMetaMaskSigner()
  return signer.getAddress()
}

export function toContractDuration(hours) {
  const numericHours = Number(hours || 0)

  if (!Number.isFinite(numericHours) || numericHours < 0) {
    throw new Error('服务时长格式不正确')
  }

  // 合约内部不直接存储浮点小时数，而是统一转换为“分钟 × 100”的整数。
  // 这样可以避免 Solidity 中浮点精度问题。
  // 例如：1.5 小时 -> 90 分钟 -> 9000
  return BigInt(Math.round(numericHours * 60 * 100))
}

export async function getVolunteerHoursContract() {
  const contractAddress = getStoredContractAddress()

  if (!contractAddress) {
    throw new Error('合约地址未配置')
  }

  // 这里使用 signer 构造合约实例，表示该实例既能读链，也能直接发起写链交易。
  // 如果只传 provider，则只能调用只读方法，不能调用 addActivity 这类写入方法。
  const signer = await getMetaMaskSigner()
  return new Contract(contractAddress, VOLUNTEER_HOURS_ABI, signer)
}

async function loadSolcCompiler() {
  const solcModule = await import('solc')
  return solcModule.default || solcModule
}

export async function compileVolunteerHoursContract() {
  const solc = await loadSolcCompiler()
  const input = {
    language: 'Solidity',
    sources: {
      'VolunteerHours.sol': {
        content: VOLUNTEER_HOURS_SOURCE
      }
    },
    settings: {
      optimizer: {
        enabled: true,
        runs: 200
      },
      outputSelection: {
        '*': {
          '*': ['abi', 'evm.bytecode.object']
        }
      }
    }
  }

  const output = JSON.parse(solc.compile(JSON.stringify(input)))
  const errors = output.errors || []
  const fatalErrors = errors.filter(item => item.severity === 'error')

  if (fatalErrors.length > 0) {
    throw new Error(fatalErrors.map(item => item.formattedMessage).join('\n'))
  }

  const contract = output.contracts?.['VolunteerHours.sol']?.VolunteerHours

  if (!contract?.evm?.bytecode?.object) {
    throw new Error('合约编译成功，但未生成字节码')
  }

  return {
    abi: contract.abi,
    bytecode: `0x${contract.evm.bytecode.object}`
  }
}

export async function formatTransactionReceipt(receipt, provider, extra = {}) {
  // 交易回执是答辩时很重要的证明材料。
  // 这里统一整理出交易哈希、区块号、时间戳、Gas、调用方、合约地址等关键字段，
  // 便于页面展示“本次写链已经成功被区块链确认”。
  const block = receipt?.blockNumber ? await provider.getBlock(receipt.blockNumber) : null

  return {
    transactionHash: receipt?.hash || receipt?.transactionHash || '',
    blockNumber: receipt?.blockNumber ?? null,
    timestamp: block?.timestamp ?? null,
    contractAddress: receipt?.contractAddress || extra.contractAddress || '',
    from: receipt?.from || extra.from || '',
    to: receipt?.to || extra.to || '',
    gasUsed: receipt?.gasUsed ? receipt.gasUsed.toString() : '',
    status: typeof receipt?.status === 'number' ? receipt.status : 1,
    chainId: CURRENT_NETWORK.chainId
  }
}

export async function deployVolunteerHoursContract() {
  // 该函数用于前端直接部署合约：
  // 1. 先编译 Solidity 源码
  // 2. 再通过 MetaMask 当前账户签名部署交易
  // 3. 等待链上确认后拿到合约地址
  const { abi, bytecode } = await compileVolunteerHoursContract()
  const signer = await getMetaMaskSigner()
  const provider = await getMetaMaskProvider()
  const factory = new ContractFactory(abi, bytecode, signer)
  const contract = await factory.deploy()
  const deploymentTx = contract.deploymentTransaction()

  if (!deploymentTx) {
    throw new Error('部署交易创建失败')
  }

  const receipt = await deploymentTx.wait()

  if (!receipt?.contractAddress) {
    throw new Error('部署完成，但未获取到合约地址')
  }

  saveContractAddress(receipt.contractAddress)

  return formatTransactionReceipt(receipt, provider, {
    contractAddress: receipt.contractAddress,
    from: await signer.getAddress(),
    to: ''
  })
}

//上链：同步时长上链
export async function sendAddActivityTransaction({ activityName, volunteerAddress, hours }) {
  if (!activityName) {
    throw new Error('活动名称不能为空')
  }

  if (!volunteerAddress) {
    throw new Error('志愿者钱包地址不能为空')
  }

  // 1. 获取合约实例
  // 2. 将页面中的小时数转换为合约需要的整数格式
  // 3. 调用合约 addActivity 写入“正常服务时长”
  // 4. 使用 tx.wait() 等待区块确认，确保这笔交易真正上链成功
  const contract = await getVolunteerHoursContract()
  const provider = await getMetaMaskProvider()
  const signer = await getMetaMaskSigner()
  const tx = await contract.addActivity(activityName, volunteerAddress, toContractDuration(hours))
  const receipt = await tx.wait()

  // 返回标准化后的交易回执，供页面展示“交易哈希、区块号、Gas”等链上凭证。
  return formatTransactionReceipt(receipt, provider, {
    from: await signer.getAddress(),
    to: getStoredContractAddress()
  })
}

//上链：补录时长上链
export async function sendAddReplenishActivityTransaction({ activityName, volunteerAddress, hours }) {
  if (!activityName) {
    throw new Error('活动名称不能为空')
  }

  if (!volunteerAddress) {
    throw new Error('志愿者钱包地址不能为空')
  }

  // 与普通活动上链逻辑相同，只是调用的是 addReplenishActivity，
  // 用于写入“补录时长”。这样在链上可以区分正常服务记录与补录记录。
  const contract = await getVolunteerHoursContract()
  const provider = await getMetaMaskProvider()
  const signer = await getMetaMaskSigner()
  const tx = await contract.addReplenishActivity(activityName, volunteerAddress, toContractDuration(hours))
  const receipt = await tx.wait()

  return formatTransactionReceipt(receipt, provider, {
    from: await signer.getAddress(),
    to: getStoredContractAddress()
  })
}

/**
 * 监听账户变化
 */
export function onAccountsChanged(callback) {
  if (window.ethereum) {
    window.ethereum.on('accountsChanged', callback)
  }
}

/**
 * 监听网络变化
 */
export function onChainChanged(callback) {
  if (window.ethereum) {
    window.ethereum.on('chainChanged', callback)
  }
}

export default {
  VOLUNTEER_HOURS_ABI,
  NETWORK_CONFIG,
  CURRENT_NETWORK,
  CONTRACT_ADDRESS,
  getStoredContractAddress,
  saveContractAddress,
  clearStoredContractAddress,
  checkMetaMaskInstalled,
  connectMetaMask,
  switchNetwork,
  getCurrentChainId,
  getMetaMaskProvider,
  getMetaMaskSigner,
  getMetaMaskAddress,
  getVolunteerHoursContract,
  compileVolunteerHoursContract,
  deployVolunteerHoursContract,
  formatTransactionReceipt,
  toContractDuration,
  sendAddActivityTransaction,
  sendAddReplenishActivityTransaction,
  onAccountsChanged,
  onChainChanged
}
