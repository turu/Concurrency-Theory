-module(cw3).
-export([start/0]).
-export([a_run/0, b_run/0, c_run/0]).

start() ->
         register(prA, spawn(?MODULE, a_run, [])),
         register(prB, spawn(?MODULE, b_run, [])),
	 register(prC, spawn(?MODULE, c_run, [])).
 
 a_run() ->
         prC ! aaa,
	 a_run().
 
 b_run() ->
         prC ! bbb,
	 b_run().

c_run() ->
	receive
		aaa ->
			io:format("aaa"),
			c_run();
		bbb ->
			io:format("bbb"),
			c_run();
		_ ->
			c_run()
	end.

