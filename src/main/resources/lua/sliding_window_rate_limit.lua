local key = KEYS[1]
local window = tonumber(ARGV[1])
local max_request_count = tonumber(ARGV[2])
local amount = tonumber(ARGV[3])

local current_time = redis.call('time')
local trim_time = tonumber(current_time[1])

redis.call('ZREMRANGEBYSCORE', key, 0, trim_time - window)
local requests = redis.call('ZCARD', key)

if requests + amount <= max_request_count then
    redis.call('ZADD', key, current_time[1], current_time[1] .. current_time[2])
    redis.call('EXPIRE', key, window)
    return false
end
return true