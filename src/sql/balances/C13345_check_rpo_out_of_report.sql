drop table if exists ${targetTestDB}.${targetOutReportTable};
create table ${targetTestDB}.${targetOutReportTable}
stored as orc
as select out_of_rep.bar_code  from (
SELECT DISTINCT ri.bar_code FROM ${sourceMergeDB}.rpo_info ri
 LEFT JOIN ( SELECT mm.bar_code FROM ${sourceMergeDB}.matreshka_merge mm where ( mm.oper_date_time >= ${start_date} AND mm.oper_date_time <= ${date} )
 AND ( mm.oper_type IN ( 2, 10, 16, 18, 21, 3000 ) OR ( mm.oper_type = 8 AND mm.oper_attr= 8 ))) T
 ON (T.bar_code = ri.bar_code)
 WHERE T.bar_code IS NULL limit 5 ) out_of_rep
 left join ${targetBalancesDB}.balance_fact bf
 where out_of_rep.bar_code = bf.bar_code
 and bf.bar_code is null ;