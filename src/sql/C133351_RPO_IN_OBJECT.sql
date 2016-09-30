
drop table if exists ${targetTestDB}.case_13351;
create table ${targetTestDB}.case_13351
stored as avro
as select distinct t1.bar_code from (
 select * from (
 select distinct opr.bar_code, opr.oper_date_time, opr.index_oper, opr.date, opr.oper_type, opr.oper_attr,
 RANK() OVER (PARTITION BY opr.bar_code ORDER BY opr.oper_date_time desc ) as rank
 FROM ${sourceMergeDB}.matreshka_merge opr ) as opr2
 where opr2.rank = 1 ) t1
 JOIN ${sourceMergeDB}.rpo_info ri
 ON t1.bar_code = ri.bar_code
 JOIN (select * from ${sourceMergeDB}.matreshka_merge ) mm2
 ON mm2.bar_code = t1.bar_code
 where (( t1.oper_type = 8 AND  t1.oper_attr = 2) OR (t1.oper_type = 12) OR (t1.oper_type = 7) OR (t1.oper_type = 23))
 AND (t1.oper_date_time >= '2015-12-01' AND t1.oper_date_time < '2015-12-31' )
 AND ( mm2.oper_type = 1 OR mm2.oper_type = 9 )
 limit 5;