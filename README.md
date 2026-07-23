# VolunteerServiceSystem
毕业设计——基于区块链的志愿者服务记录系统设计与实现
项目技术:SpringBoot、MySQL、SpringMVC、Spring Security、MyBatis、Vue3、ElementPlus、Axios、Web3j（区块链）、Solidity、JWT
项目描述:基于区块链的志愿者服务系统是一款面向志愿者提供活动浏览、报名、签到、时长记录、积分兑换、证书管理、社区交流等服务的综合性平台，项目分为用户端与管理端。（项目已提交至gitee：https://gitee.com/old-oak/volunteer-service-system.git）
用户端：活动浏览与筛选、报名签到、时长与积分管理、积分兑换商城、证书下载与链上存证、社区互动。
管理端：活动与志愿者管理、签到记录、积分补录审核、证书模板管理、权限管理。
系统亮点：
采用 MyBatis 动态SQL 实现多条件活动筛选（时间、地点、分类），结合分页查询提升大规模数据检索性能。
基于 PointsCalculationService 统一计算积分（活动积分+补录积分-兑换积分+退款积分），实现积分流转的精确管理。
采用 Web3j 集成区块链技术 实现志愿者服务证书的链上存证，确保服务记录不可篡改，提升证书可信度。
