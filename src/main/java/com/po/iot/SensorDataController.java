package com.po.iot;

import static com.po.iot.brewdata.public_.tables.Bubbledata.BUBBLEDATA;
import static com.po.iot.brewdata.public_.tables.Environmentdata.ENVIRONMENTDATA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.po.iot.brewdata.public_.tables.records.BubbledataRecord;
import com.po.iot.brewdata.public_.tables.records.EnvironmentdataRecord;

@RestController
public class SensorDataController {
	
	@Autowired DSLContext dsl;

	@RequestMapping(value = "/env", method = RequestMethod.POST)
	public void envData(@RequestParam(value="temp") double temp, @RequestParam(value="hum") double hum, HttpServletRequest request) {
		// Allow only hosts from local network to POST.
		if ( ! request.getRemoteAddr().startsWith("192.168.") && ! request.getRemoteAddr().startsWith("127.0.0.1") ) {
			throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
		}
		System.out.println(new java.util.Date() + " Environment data!");
		int nr = dsl.insertInto(ENVIRONMENTDATA, ENVIRONMENTDATA.TIMESTAMP, ENVIRONMENTDATA.TEMPERATURE, ENVIRONMENTDATA.HUMIDITY)
		.values(System.currentTimeMillis(), BigDecimal.valueOf(temp), BigDecimal.valueOf(hum)).execute();
		if (nr == 1) {
			return;
		} else {
			throw new HttpServerErrorException(HttpStatus.EXPECTATION_FAILED, "Expected to insert 1 row, instead " + nr + " were inserted.");
		}
	}
	
	@RequestMapping(value = "/bubble", method = RequestMethod.POST)
	public String bubble(HttpServletRequest request) {
		// Allow only hosts from local network to POST.
		if ( ! request.getRemoteAddr().startsWith("192.168.") && ! request.getRemoteAddr().startsWith("127.0.0.1") ) {
			throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
		}
		System.out.println(new java.util.Date() + " Bubble!");
		int nr = dsl.insertInto(BUBBLEDATA, BUBBLEDATA.TIMESTAMP)
				.values(System.currentTimeMillis()).execute();
		if (nr == 1) {
			return "OK";
		} else {
			throw new HttpServerErrorException(HttpStatus.EXPECTATION_FAILED, "Expected to insert 1 row, instead " + nr + " were inserted.");
		}
	}
	
	@RequestMapping(value = "/temperature", method = RequestMethod.GET)
	public TemperatureDataList temperature(@RequestParam(value="hours", defaultValue="48") long hours) {
		long from = System.currentTimeMillis() - hours * 60 * 60 * 1000;
		Result<EnvironmentdataRecord> data = dsl.selectFrom(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.greaterOrEqual(from)).orderBy(ENVIRONMENTDATA.TIMESTAMP).fetch();
		List<Object[]> temperatures = new ArrayList<Object[]>();
		for (EnvironmentdataRecord rec : data) {
			temperatures.add(new Object[]{rec.get(ENVIRONMENTDATA.TIMESTAMP), rec.get(ENVIRONMENTDATA.TEMPERATURE).doubleValue(), rec.get(ENVIRONMENTDATA.HUMIDITY).doubleValue()});
		}
		return new TemperatureDataList(temperatures);
	}
	
	@RequestMapping(value = "/bubbles", method = RequestMethod.GET)
	public BubbleDataList bubbles(@RequestParam(value="hours", defaultValue="48") long hours) {
		long from = System.currentTimeMillis() - hours * 60 * 60 * 1000;
		Result<BubbledataRecord> data = dsl.selectFrom(BUBBLEDATA).where(BUBBLEDATA.TIMESTAMP.greaterOrEqual(from)).orderBy(BUBBLEDATA.TIMESTAMP).fetch();
		List<Object[]> bubbleTimestamps = new ArrayList<Object[]>();
		for (BubbledataRecord rec : data) {
			bubbleTimestamps.add(new Object[]{rec.get(BUBBLEDATA.TIMESTAMP), 1});
		}
		return new BubbleDataList(bubbleTimestamps);
	}
	
	@RequestMapping(value = "/joined", method = RequestMethod.GET)
	public DataList joined(@RequestParam(value="hours", defaultValue="48") long hours) {
		long from = System.currentTimeMillis() - hours * 60 * 60 * 1000;
		Result<Record3<Long, Integer, BigDecimal>> data = dsl
				.select(
						BUBBLEDATA.TIMESTAMP,
						DSL.val(1).as("bubble"),
						DSL.val(null, BigDecimal.class).as(ENVIRONMENTDATA.TEMPERATURE)
					).from(BUBBLEDATA).where(BUBBLEDATA.TIMESTAMP.greaterOrEqual(from))
				.unionAll(DSL.select(
						ENVIRONMENTDATA.TIMESTAMP.as(BUBBLEDATA.TIMESTAMP),
						DSL.val(null, Integer.class).as("bubble"),
						ENVIRONMENTDATA.TEMPERATURE
					).from(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.greaterOrEqual(from)))
				.orderBy(DSL.one().desc()).fetch();
		List<Object[]> bubbleTimestamps = new ArrayList<Object[]>();
		for (Record3<Long, Integer, BigDecimal> rec : data) {
			bubbleTimestamps.add(new Object[]{rec.get(BUBBLEDATA.TIMESTAMP), rec.get(ENVIRONMENTDATA.TEMPERATURE), rec.getValue("bubble")});
		}
		return new DataList(bubbleTimestamps);
	}
	
	@RequestMapping(value = "/last", method = RequestMethod.GET)
	public EnvData last() {
		long maxTs = dsl.select(DSL.max(ENVIRONMENTDATA.TIMESTAMP)).from(ENVIRONMENTDATA).fetch().get(0).value1();
		EnvironmentdataRecord latestEnvData = dsl.selectFrom(ENVIRONMENTDATA).where(ENVIRONMENTDATA.TIMESTAMP.equal(maxTs)).fetchAny();
		BubbledataRecord latestBubbleData = dsl.selectFrom(BUBBLEDATA).orderBy(BUBBLEDATA.TIMESTAMP.desc()).fetchAny();
		return new EnvData(latestEnvData.getTimestamp(), latestEnvData.getTemperature().doubleValue(), latestEnvData.getHumidity().doubleValue(), latestBubbleData.getTimestamp());
	}
}
