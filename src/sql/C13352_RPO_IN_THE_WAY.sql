drop table if exists ${targetTestDB}.case_C13352;
create table ${targetTestDB}.case_C13352
stored as avro
as select distinct t1.bar_code from (
    select opr2.* from (
    select distinct opr.bar_code, opr.oper_date_time, opr.index_next,  opr.index_oper, opr.date, opr.oper_type, opr.oper_attr,
    RANK() OVER (PARTITION BY opr.bar_code ORDER BY opr.oper_date_time desc ) as rank
    FROM ${sourceMergeDB}.matreshka_merge opr ) as opr2
    where opr2.rank = 1 ) t1
    LEFT JOIN ${sourceMergeDB}.rpo_info ri
    ON t1.bar_code = ri.bar_code
    LEFT JOIN ${dictsDB}.fps_structure fps_oper ON ( fps_oper.post_object_index = t1.index_oper )
    LEFT JOIN ${dictsDB}.fps_structure fps_next ON (fps_next.post_object_index = t1.index_next )
    LEFT JOIN (select * from ${sourceMergeDB}.matreshka_merge ) mm2
    ON mm2.bar_code = t1.bar_code
    where ( t1.oper_type = 8 AND t1.oper_attr in (4, 6, 8)) OR (t1.oper_type in  (13, 18, 1018, 1027, 1031))
    AND ( mm2.oper_type = 1 OR mm2.oper_type = 9 )
    AND ( fps_oper.object_border_index <> fps_next.object_border_index )
    AND (mm2.oper_date_time >= '2015-12-01' AND mm2.oper_date_time < '2016-12-31' )
    AND (t1.oper_date_time >= '2015-12-01' AND t1.oper_date_time < '2015-12-31')
    limit 5;