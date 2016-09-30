/*результат данные РПО в эталонную таблицу */
drop table test_auto_merge.matreshka_merge;

create table test_auto_merge.matreshka_merge stored as orc as
select * from merge.matreshka_merge
where bar_code
in (
'RT874864703DE',
'RS336844735NL',
'RR030960165VN',
'RR006230642SD',
'RQ170126283UZ',

'RR943098545CZ',
'VA003360952RU',
'RX534700495DE',
'RW388444305FR',
'RW268256305FR',

'10100063011085',
'10100087612596',
'10100091524991',
'10100089381346',
'10100089384033'
 )
and ( oper_date_time >= '2015-12-01' AND oper_date_time < '2016-01-01' ) ;