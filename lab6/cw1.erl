-module(cw1).
-export([start/0]).
-export([a_run/0, b_run/0]).

start() ->
	register(prA, spawn(?MODULE, a_run, [])),
	register(prB, spawn(?MODULE, b_run, [])).

a_run() ->
	receive
		hello ->
			prB ! hello,
			a_run()
	end.

b_run() ->
	receive
		hello ->
			io:format("hello"),
			b_run();
		killme ->
			io:format("i was killed")
	end.

