--string's key
local stringKey = KEYS[1]
--string's value
local stringVal = ARGV[1]
--expiration time
local expireAt = ARGV[2]
--check value's existence,initialize
local keyExist = redis.call("SETNX",KEYS[1],stringVal);
if (keyExist >= 1) then
    --EXPIREAT:时间戳
    --EXPIRE:
    redis.call("EXPIRE",KEYS[1],expireAt)
    return true
 end
return false
 --return newest results
 --return tonumber(keyExist)