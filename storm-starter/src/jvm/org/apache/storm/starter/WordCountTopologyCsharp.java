/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.starter;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.ShellSpout;
import org.apache.storm.task.ShellBolt;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.Map;

/**
 * This topology demonstrates Storm's stream groupings and multilang capabilities.
 */
public class WordCountTopologyCsharp {
	public static class Generator extends ShellSpout implements IRichSpout {

		public Generator() {
			super("mono", "StormSample.exe", "generator");
			
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}
	}	
	
	public static class Splitter extends ShellBolt implements IRichBolt {

		public Splitter() {
			super("mono", "StormSample.exe", "splitter");
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}
	}
	
	public static class Counter extends ShellBolt implements IRichBolt {
		
		public Counter(){
			super("mono", "StormSample.exe", "counter");
		}
		
		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}
	}
	

	public static void main(String[] args) throws Exception {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("generator", new Generator(), 1);

		builder.setBolt("splitter", new Splitter(), 5).fieldsGrouping("generator",
				new Fields("word"));
		
		builder.setBolt("counter", new Counter(), 8).fieldsGrouping("splitter",
				new Fields("word", "count"));

		Config conf = new Config();
		//conf.setDebug(true);
		

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);

			StormSubmitter.submitTopologyWithProgressBar(args[0], conf,
					builder.createTopology());
		} else {
			conf.setMaxTaskParallelism(3);

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("WordCount", conf, builder.createTopology());

			Thread.sleep(10000);

			cluster.shutdown();
		}
	}
}