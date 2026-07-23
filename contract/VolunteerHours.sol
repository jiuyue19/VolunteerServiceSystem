// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @title VolunteerHours
 * @dev 志愿者服务时长管理合约
 * @notice 所有时长以分钟为单位存储（乘以100以保留两位小数）
 *         例如：1小时30分 = 90.00分钟 = 9000
 */
contract VolunteerHours {

    // 管理员地址
    address public admin;

    // 志愿者总时长（非补录）
    mapping(address => uint256) public totalMinutes;

    // 志愿者补录时长
    mapping(address => uint256) public replenishMinutes;

    // 志愿者活动记录列表
    mapping(address => ActivityRecord[]) private activityRecords;

    // 活动记录结构体
    struct ActivityRecord {
        string activityName;     // 活动名称
        uint256 duration;        // 服务时长（分钟 × 100）
        uint256 timestamp;       // 上链时间戳
        bool isReplenish;        // 是否为补录
    }

    // 事件：记录总时长更新
    event DurationUpdated(
        address indexed volunteer,
        uint256 totalMinutes,
        uint256 timestamp
    );

    // 事件：记录补录活动
    event DurationReplenished(
        address indexed volunteer,
        uint256 replenishDuration,
        uint256 timestamp,
        string activityName
    );

    // 事件：添加活动记录
    event RecordAdded(
        address indexed volunteer,
        uint256 duration,
        bool isReplenish,
        string activityName,
        uint256 timestamp
    );

    // 修饰符：仅管理员可调用
    modifier onlyAdmin() {
        require(msg.sender == admin, "Only admin can call this function");
        _;
    }

    /**
     * @dev 构造函数：部署合约者为管理员
     */
    constructor() {
        admin = msg.sender;
    }

    /**
     * @dev 添加单条活动记录（非补录）
     * @param activityName 活动名称
     * @param volunteer 志愿者地址
     * @param duration 服务时长（分钟 × 100）
     */
    function addActivity(
        string memory activityName,
        address volunteer,
        uint256 duration
    ) public onlyAdmin {
        require(volunteer != address(0), "Invalid volunteer address");
        require(bytes(activityName).length > 0, "Activity name cannot be empty");
        require(duration > 0, "Duration must be greater than 0");

        // 更新总时长
        totalMinutes[volunteer] += duration;

        // 添加活动记录
        activityRecords[volunteer].push(ActivityRecord(activityName, duration, block.timestamp, false));

        // 触发事件
        emit RecordAdded(volunteer, duration, false, activityName, block.timestamp);
        emit DurationUpdated(volunteer, totalMinutes[volunteer], block.timestamp);
    }

    /**
     * @dev 批量添加活动记录（非补录）
     * @param activityNames 活动名称数组
     * @param volunteers 志愿者地址数组
     * @param durations 服务时长数组
     */
    function batchAddActivities(
        string[] memory activityNames,
        address[] memory volunteers,
        uint256[] memory durations
    ) public onlyAdmin {
        require(activityNames.length == volunteers.length && volunteers.length == durations.length, "Arrays length mismatch");

        // 循环调用单条添加
        for (uint256 i = 0; i < volunteers.length; i++) {
            addActivity(activityNames[i], volunteers[i], durations[i]);
        }
    }

    /**
     * @dev 添加单条补录活动记录
     * @param activityName 活动名称
     * @param volunteer 志愿者地址
     * @param duration 补录时长（分钟 × 100）
     */
    function addReplenishActivity(
        string memory activityName,
        address volunteer,
        uint256 duration
    ) public onlyAdmin {
        require(volunteer != address(0), "Invalid volunteer address");
        require(bytes(activityName).length > 0, "Activity name cannot be empty");
        require(duration > 0, "Duration must be greater than 0");

        // 更新补录和总时长
        replenishMinutes[volunteer] += duration;
        totalMinutes[volunteer] += duration;

        // 添加补录活动记录
        activityRecords[volunteer].push(ActivityRecord(activityName, duration, block.timestamp, true));

        // 触发事件
        emit DurationReplenished(volunteer, duration, block.timestamp, activityName);
        emit RecordAdded(volunteer, duration, true, activityName, block.timestamp);
        emit DurationUpdated(volunteer, totalMinutes[volunteer], block.timestamp);
    }

    /**
     * @dev 批量添加补录活动记录
     * @param activityNames 活动名称数组
     * @param volunteers 志愿者地址数组
     * @param durations 补录时长数组
     */
    function batchAddReplenishActivities(
        string[] memory activityNames,
        address[] memory volunteers,
        uint256[] memory durations
    ) public onlyAdmin {
        require(activityNames.length == volunteers.length && volunteers.length == durations.length, "Arrays length mismatch");

        for (uint256 i = 0; i < volunteers.length; i++) {
            addReplenishActivity(activityNames[i], volunteers[i], durations[i]);
        }
    }

    /**
     * @dev 查询总服务时长（分钟 × 100）
     */
    function getTotalMinutes(address volunteer) public view returns (uint256) {
        return totalMinutes[volunteer];
    }

    /**
     * @dev 查询总服务时长（小时）
     */
    function getTotalHours(address volunteer) public view returns (uint256) {
        return totalMinutes[volunteer] / 6000;
    }

    /**
     * @dev 查询补录时长（分钟 × 100）
     */
    function getReplenishMinutes(address volunteer) public view returns (uint256) {
        return replenishMinutes[volunteer];
    }

    /**
     * @dev 查询补录时长（小时）
     */
    function getReplenishHours(address volunteer) public view returns (uint256) {
        return replenishMinutes[volunteer] / 60;
    }

    /**
     * @dev 查询志愿者活动记录数量
     */
    function getRecordCount(address volunteer) public view returns (uint256) {
        return activityRecords[volunteer].length;
    }

    /**
     * @dev 查询指定索引的活动记录
     */
    function getRecord(address volunteer, uint256 index)
        public view returns (
            string memory activityName,
            uint256 duration,
            uint256 timestamp,
            bool isReplenish
        )
    {
        require(index < activityRecords[volunteer].length, "Index out of bounds");
        ActivityRecord memory record = activityRecords[volunteer][index];
        return (record.activityName, record.duration, record.timestamp, record.isReplenish);
    }

    /**
     * @dev 查询所有活动记录
     */
    function getAllRecords(address volunteer) public view returns (ActivityRecord[] memory) {
        return activityRecords[volunteer];
    }

    /**
     * @dev 转移管理员权限
     */
    function transferAdmin(address newAdmin) public onlyAdmin {
        require(newAdmin != address(0), "Invalid admin address");
        admin = newAdmin;
    }
}