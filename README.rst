Elasticsearch Gatherer Plugin
=============================

The gatherer plugin for Elasticsearch is a framework for scalable data fetching and indexing.
Content adapters are implemented in gatherer zip archives which are a special kind of plugins
distributable over Elasticsearch nodes. They can receive job requests and execute them in local
queues. Job states are maintained in a special index.

This plugin is under development.

Milestone 1 - deploy gatherer zips to nodes

Milestone 2 - job specification and execution

Milestone 3 - porting JDBC river to JDBC gatherer

Milestone 4 - gatherer job distribution by load/queue length/node name, cron jobs

Milestone 5 - more gatherers, more content adapters

Milestone 1 is reached: basic deployment of gatherer zips works::

    curl -XPOST '0:9200/_deploy?name=test&path=/tmp/elasticsearch-gatherer-0.90.9.1.zip'
    {"deploy":true}

    curl '0:9200/_nodes?pretty'
    {
      "ok" : true,
      "cluster_name" : "elasticsearch",
      "nodes" : {
        "5gMqZ00JQ72RhnUdBnDc6g" : {
          "name" : "Thinker",
          "transport_address" : "inet[/192.168.1.113:9300]",
          "hostname" : "Jorg-Prantes-MacBook-Pro.local",
          "version" : "0.90.9",
          "gatherer.length" : "0",
          "http_address" : "inet[/192.168.1.113:9200]",
          "gatherer.modules" : "test/dummy-gatherer",
          "gatherer.load" : "2.4326171875"
        }
      }
    }

Installation
------------

Prerequisites::

  Elasticsearch 0.90+

=============  =========  =================  ===========================================================
ES version     Plugin     Release date       Command
-------------  ---------  -----------------  -----------------------------------------------------------
0.90.9         0.90.9.1                       ./bin/plugin --install gatherer --url
=============  =========  =================  ===========================================================

Do not forget to restart the node after installing.

Project docs
------------

The Maven project site is available at `Github <http://jprante.github.io/elasticsearch-gatherer>`_

Binaries
--------

Binaries are available at `Bintray <https://bintray.com/pkg/show/general/jprante/elasticsearch-plugins/elasticsearch-gatherer>`_

Overview
--------

.. image:: ../../../elasticsearch-gatherer/raw/master/src/site/resources/gatherer-diagram.png


Credits
=======

This program contains modified source code taken from Apache Geronimo (URIClassLoader) and
Quartz Scheduler (Cron Expression parser).

License
=======

Elasticsearch Gatherer Plugin

Copyright (C) 2014 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

