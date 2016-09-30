SELECT DISTINCT ri.bar_code FROM merge.rpo_info ri
LEFT JOIN ( SELECT mm.bar_code FROM merge.matreshka_merge mm where ( mm.oper_date_time >= '2015-12-01' AND mm.oper_date_time <= '2015-12-31' )
AND ( mm.oper_type IN ( 2, 10, 16, 18, 21, 3000 ) OR ( mm.oper_type = 8 AND mm.oper_attr= 8 ))) T
ON (T.bar_code = ri.bar_code)
WHERE T.bar_code IS NULL limit 5;
