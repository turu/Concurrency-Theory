-module(producers).
-export([main/0, producent/0, konsument/0, buffer/2]).

main() ->
	spawn(producers, producent, []),
	spawn(producers, producent, []),
	spawn(producers, producent, []),
	register(bufor, spawn(producers, bufor, [[],3])),
	spawn(producers, konsument, []),
	receive
		Msg -> io:format("~w~n", [Msg])
	end.

konsument() ->
	bufor ! {self(), bedzie_konsumowac},
	receive
		Produkt -> io:format("~w: konsumpcja ~w~n", [self(), Produkt])
	end,
	konsument().

producent() ->
	bufor ! {self(), bedzie_produkowac},
	receive
		{moze_produkowac} -> 
			io:format("~w: produkcja ~n", [self()]),
			bufor ! {prod, produkt}
	end,
	producent().

bufor(Lista, Limit) ->
	io:format("~w: w stanie ~w~n", [self(), Lista]),
	receive
		{ProducentPid, bedzie_produkowac} when length(Lista) < Limit ->
			ProducentPid ! {moze_produkowac},
	receive
		{prod, Produkt} -> 
			io:format("~w: otrzymuje ~w~n",[self(), {Produkt}]),
			bufor([Produkt|Lista], Limit)	
	end;
	{KonsumentPid, bedzie_konsumowac} when length(Lista) > 0 ->
		[A | B] = Lista,
		KonsumentPid ! {A},
		bufor(B, Limit)	
	end.

