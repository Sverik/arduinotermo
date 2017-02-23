package com.po.iot;

import static com.po.iot.brewdata.public_.tables.Bubbledata.BUBBLEDATA;
import static com.po.iot.brewdata.public_.tables.Environmentdata.ENVIRONMENTDATA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.po.iot.brewdata.public_.tables.records.EnvironmentdataRecord;

@RestController
public class SensorDataController {
	
	@Autowired DSLContext dsl;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		Record1<Integer> timestamp = dsl.select(BUBBLEDATA.TIMESTAMP).from(BUBBLEDATA).fetchAny();
		int ts = timestamp.get(BUBBLEDATA.TIMESTAMP);
		return new Greeting(ts, "kala " + name);
	}
	
	@RequestMapping(value = "/env", method = RequestMethod.POST)
	public PostEnvDataResponse envData(@RequestParam(value="temp") double temp, @RequestParam(value="hum") double hum, HttpServletRequest request) {
		// Allow only hosts from local network to POST.
		if ( ! request.getRemoteAddr().startsWith("192.168.")) {
			throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
		}
		int nr = dsl.insertInto(ENVIRONMENTDATA, ENVIRONMENTDATA.TIMESTAMP, ENVIRONMENTDATA.TEMPERATURE, ENVIRONMENTDATA.HUMIDITY)
		.values(System.currentTimeMillis(), BigDecimal.valueOf(temp), BigDecimal.valueOf(hum)).execute();
		if (nr == 1) {
			return new PostEnvDataResponse();
		} else {
			throw new HttpServerErrorException(HttpStatus.EXPECTATION_FAILED, "Expected to insert 1 row, instead " + nr + " were inserted.");
		}
	}
	
	@RequestMapping(value = "/temperature", method = RequestMethod.GET)
	public TemperatureDataList temperature(@RequestParam(value="hours", defaultValue="48") long hours) {
		long from = System.currentTimeMillis() - hours * 60 * 60 * 1000;
		Result<EnvironmentdataRecord> data = dsl.selectFrom(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.greaterOrEqual(from)).orderBy(ENVIRONMENTDATA.TIMESTAMP).fetch();
		List<long[]> temperatures = new ArrayList<long[]>();
		for (EnvironmentdataRecord rec : data) {
			temperatures.add(new long[]{rec.get(ENVIRONMENTDATA.TIMESTAMP), (long)(rec.get(ENVIRONMENTDATA.TEMPERATURE).doubleValue() * 10d)});
		}
		return new TemperatureDataList(temperatures);
	}
	
	@RequestMapping(value = "/last", method = RequestMethod.GET)
	public EnvData last() {
		long maxTs = dsl.select(DSL.max(ENVIRONMENTDATA.TIMESTAMP)).from(ENVIRONMENTDATA).fetch().get(0).value1();
		EnvironmentdataRecord latestEnvData = dsl.selectFrom(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.equal(maxTs)).fetchAny();
		return new EnvData(latestEnvData.getTimestamp(), latestEnvData.getTemperature().doubleValue(), latestEnvData.getHumidity().doubleValue());
	}
}
