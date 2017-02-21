package com.po.iot;

import static com.po.iot.brewdata.public_.tables.Bubbledata.BUBBLEDATA;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
	
	@Autowired DSLContext dsl;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		Record1<Integer> timestamp = dsl.select(BUBBLEDATA.TIMESTAMP).from(BUBBLEDATA).fetchAny();
		int ts = timestamp.get(BUBBLEDATA.TIMESTAMP);
		return new Greeting(ts, "kala " + name);
	}
}
