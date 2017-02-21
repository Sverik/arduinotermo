package com.po.iot;

import static com.po.iot.brewdata.public_.tables.Bubbledata.BUBBLEDATA;
import static com.po.iot.brewdata.public_.tables.Environmentdata.ENVIRONMENTDATA;

import java.math.BigDecimal;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.po.iot.brewdata.public_.tables.Environmentdata;
import com.po.iot.brewdata.public_.tables.records.EnvironmentdataRecord;

@RestController
public class GreetingController {
	
	@Autowired DSLContext dsl;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		Record1<Integer> timestamp = dsl.select(BUBBLEDATA.TIMESTAMP).from(BUBBLEDATA).fetchAny();
		int ts = timestamp.get(BUBBLEDATA.TIMESTAMP);
		return new Greeting(ts, "kala " + name);
	}
	
	@RequestMapping(value = "/env", method = RequestMethod.POST)
	public Greeting greeting(@RequestParam(value="temp") double temp, @RequestParam(value="hum") double hum) {
		int nr = dsl.insertInto(ENVIRONMENTDATA, ENVIRONMENTDATA.TIMESTAMP, ENVIRONMENTDATA.TEMPERATURE, ENVIRONMENTDATA.HUMIDITY)
		.values(System.currentTimeMillis(), BigDecimal.valueOf(temp), BigDecimal.valueOf(hum)).execute();
		return new Greeting(nr, "ok");
	}
	
	@RequestMapping(value = "/last", method = RequestMethod.GET)
	public EnvData last() {
		long maxTs = dsl.select(DSL.max(ENVIRONMENTDATA.TIMESTAMP)).from(ENVIRONMENTDATA).fetch().get(0).value1();
		EnvironmentdataRecord latestEnvData = dsl.selectFrom(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.equal(maxTs)).fetchAny();
		return new EnvData(latestEnvData.getTimestamp(), latestEnvData.getTemperature().doubleValue(), latestEnvData.getHumidity().doubleValue());
	}
}
