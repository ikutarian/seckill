-- 秒杀执行的存储过程
DELIMITER $$ -- ; 转换为 $$
-- 定义存储过程
-- 参数定义： in 表示输入参数
--          out 表示输出参数
-- row_count() 返回上一条修改类型 sql(delete, insert, update) 的影响行数
-- row_count: 0: 未修改
--            >0: 表示修改的行数
--            <0：SQL错误/未执行
CREATE PROCEDURE `seckill`.`execute_seckill` (in v_seckill_id bigint, in v_phone bigint, in v_kill_time timestamp, out r_result int)
  BEGIN
    DECLARE insert_count int DEFAULT 0;
    START TRANSACTION ;
    INSERT IGNORE INTO success_killed (seckill_id, user_phone, status, create_time) VALUES (v_seckill_id, v_phone, 0, v_kill_time);
    SELECT ROW_COUNT() INTO insert_count;
    IF (insert_count = 0) THEN -- 未修改
      ROLLBACK;
      SET r_result = -1;
    ELSEIF (insert_count < 0) THEN  -- SQL错误或者未执行
      ROLLBACK;
      SET r_result = -2;
    ELSE
      -- 执行成功，接着执行更新库存的操作
      UPDATE seckill
      SET
        number = number - 1
      WHERE
        seckill_id = v_seckill_id
        AND end_time > v_kill_time
        AND start_time < v_kill_time
        AND number > 0;
      SELECT ROW_COUNT() INTO insert_count;
        IF (insert_count = 0) THEN
          ROLLBACK;
          SET r_result = 0;  -- 秒杀已结束
        ELSEIF (insert_count < 0) THEN
          ROLLBACK;
          SET r_result = -2; -- SQL错误或者未执行
        ELSE
          COMMIT;
          SET r_result = 1;
        END IF;
    END IF;
  END;
$$


DELIMITER ;
SET @r_result = -3;
CALL execute_seckill(1000, 13812345678, now(), @r_result);
SELECT @r_result;