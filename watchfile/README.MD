WatchFile plugin for Elasticsearch 2.4.1

Installation:
Download archive and install
	bin/plugin install file://path/to/file/watchfile-x.x.x.zip

Configuration elasticsearch.yml:
Each task must defined by required options. All options must be started with watchfile.taskX, where X is number of task.

Required configuration options:
	watchfile.taskX.elastichost - IP or FQND of the elasticsearch host.
	watchfile.taskX.elasticport - port of the elasticsearch.
	watchfile.taskX.folder - folder where plugin will be trying to find documents. 
	watchfile.taskX.query - for example: '/paloalto_logs-2017.01/_search?pretty'.
	watchfile.taskX.querybody - Must contain %FILE% passphrase. For example: {\"cv\":\"%FILE%\"}.
Additional:
	watchfile.taskX.timeformat - Time format. Default "yyyy/MM/dd HH:mm:ss".

		
Reserved passphrase.
	%YEAR% - in format yyyy.
	%MONTH%  - in fromat mm, possible value 01-12	
	%DAY% - in format dd, possible value 01-31.
	%CURDATE% - see watchalert.taskX.timeformat.
	%MESSAGE% - message body. May be replaced by watchalert.taskX.message.
	%HOST% - existing host ID in EMS.
	%EPOCHTIME% - Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
	%TIMESTAMP% - in format  yyyy-MM-ddTHH:mm:ss.SSSZ.
	%TIMESTAMP-PERIOD% - Current datetime minus period.Format yyyy-MM-ddTHH:mm:ss.SSSZ.
	%FILETYPE% - 
	 