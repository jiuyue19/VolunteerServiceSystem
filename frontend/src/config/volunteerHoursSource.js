export const VOLUNTEER_HOURS_SOURCE = `// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract VolunteerHours {
    address public admin;
    mapping(address => uint256) public totalMinutes;
    mapping(address => uint256) public replenishMinutes;
    mapping(address => ActivityRecord[]) private activityRecords;

    struct ActivityRecord {
        string activityName;
        uint256 duration;
        uint256 timestamp;
        bool isReplenish;
    }

    event DurationUpdated(
        address indexed volunteer,
        uint256 totalMinutes,
        uint256 timestamp
    );

    event DurationReplenished(
        address indexed volunteer,
        uint256 replenishDuration,
        uint256 timestamp,
        string activityName
    );

    event RecordAdded(
        address indexed volunteer,
        uint256 duration,
        bool isReplenish,
        string activityName,
        uint256 timestamp
    );

    modifier onlyAdmin() {
        require(msg.sender == admin, "Only admin can call this function");
        _;
    }

    constructor() {
        admin = msg.sender;
    }

    function addActivity(
        string memory activityName,
        address volunteer,
        uint256 duration
    ) public onlyAdmin {
        require(volunteer != address(0), "Invalid volunteer address");
        require(bytes(activityName).length > 0, "Activity name cannot be empty");
        require(duration > 0, "Duration must be greater than 0");

        totalMinutes[volunteer] += duration;
        activityRecords[volunteer].push(ActivityRecord(activityName, duration, block.timestamp, false));

        emit RecordAdded(volunteer, duration, false, activityName, block.timestamp);
        emit DurationUpdated(volunteer, totalMinutes[volunteer], block.timestamp);
    }

    function batchAddActivities(
        string[] memory activityNames,
        address[] memory volunteers,
        uint256[] memory durations
    ) public onlyAdmin {
        require(activityNames.length == volunteers.length && volunteers.length == durations.length, "Arrays length mismatch");

        for (uint256 i = 0; i < volunteers.length; i++) {
            addActivity(activityNames[i], volunteers[i], durations[i]);
        }
    }

    function addReplenishActivity(
        string memory activityName,
        address volunteer,
        uint256 duration
    ) public onlyAdmin {
        require(volunteer != address(0), "Invalid volunteer address");
        require(bytes(activityName).length > 0, "Activity name cannot be empty");
        require(duration > 0, "Duration must be greater than 0");

        replenishMinutes[volunteer] += duration;
        totalMinutes[volunteer] += duration;
        activityRecords[volunteer].push(ActivityRecord(activityName, duration, block.timestamp, true));

        emit DurationReplenished(volunteer, duration, block.timestamp, activityName);
        emit RecordAdded(volunteer, duration, true, activityName, block.timestamp);
        emit DurationUpdated(volunteer, totalMinutes[volunteer], block.timestamp);
    }

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

    function getTotalMinutes(address volunteer) public view returns (uint256) {
        return totalMinutes[volunteer];
    }

    function getTotalHours(address volunteer) public view returns (uint256) {
        return totalMinutes[volunteer] / 6000;
    }

    function getReplenishMinutes(address volunteer) public view returns (uint256) {
        return replenishMinutes[volunteer];
    }

    function getReplenishHours(address volunteer) public view returns (uint256) {
        return replenishMinutes[volunteer] / 60;
    }

    function getRecordCount(address volunteer) public view returns (uint256) {
        return activityRecords[volunteer].length;
    }

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

    function getAllRecords(address volunteer) public view returns (ActivityRecord[] memory) {
        return activityRecords[volunteer];
    }

    function transferAdmin(address newAdmin) public onlyAdmin {
        require(newAdmin != address(0), "Invalid admin address");
        admin = newAdmin;
    }
}
`
