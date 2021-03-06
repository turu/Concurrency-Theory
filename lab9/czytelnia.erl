-module(czytelnia).
-export([writer/1, reader/1, reading_room/4, main/0, submit_readers/2, submit_writers/2]).

reading_room(Readers_in, Writer_writes, Writer_PID, Places_left) ->
    receive 
        {start_writing, Writer} ->
            if 
                (Writer_writes == 0) and (Readers_in == 0) ->
                    Writer ! could_write,
                    reading_room(Readers_in, 1, Writer, Places_left-1);
                (Writer_writes == 0) ->
                    reading_room(Readers_in, 1, Writer, Places_left);
                true ->
                    self() ! {start_writing, Writer},
                    reading_room(Readers_in, 1, Writer_PID, Places_left)
            end;

        {stop_writing} ->
            reading_room(Readers_in, 0, -1, Places_left+1);

        {start_reading, Reader} ->
            if
                Writer_writes == 0 ->
                    Reader ! could_read,
                    reading_room(Readers_in + 1, 0, -1, Places_left-1);
                true ->
                    self() ! {start_reading, Reader},
                    reading_room(Readers_in, Writer_writes, Writer_PID, Places_left)
            end;
            
        {stop_reading} ->
            if 
                (Readers_in == 1) and (Writer_writes == 1) ->
                    Writer_PID ! could_write,
                    reading_room(Readers_in - 1, Writer_writes, Writer_PID, Places_left+1);
                true ->
                    reading_room(Readers_in - 1, Writer_writes, Writer_PID, Places_left+1)
            end
    end.

writer(Reading_room_PID) ->
    timer:sleep(random:uniform(1000)),
    Reading_room_PID ! {start_writing, self()},
    receive
        could_write -> 
            io:fwrite("writer came to the library ~p\n", [self()])
    end,
    timer:sleep(random:uniform(1000)),
    Reading_room_PID ! {stop_writing},
    io:fwrite( "writer finished writing ~p\n", [self()]).

reader(Reading_room_PID) ->
    timer:sleep(random:uniform(1000)),
    Reading_room_PID ! {start_reading, self()},
    receive
        could_read -> 
            io:fwrite("reading started reading ~p\n", [self()])
    end,
    timer:sleep(random:uniform(1000)),
    Reading_room_PID ! {stop_reading},
    io:fwrite("reader finished reading ~p\n", [self()]).

main() ->
    Reading_room_PID = spawn(czytelnia, reading_room, [0, 0, -1, 8]),
    submit_readers(8, Reading_room_PID),
    submit_writers(4, Reading_room_PID).

submit_readers(0, Reading_room_PID) ->
    spawn(czytelnia, reader, [Reading_room_PID]);

submit_readers(N, Reading_room_PID) ->
    spawn(czytelnia, reader, [Reading_room_PID]),
    submit_readers(N-1, Reading_room_PID).

submit_writers(0, Reading_room_PID) ->
    spawn(czytelnia, writer, [Reading_room_PID]);

submit_writers(N, Reading_room_PID) ->
    spawn(czytelnia, writer, [Reading_room_PID]),
    submit_writers(N-1, Reading_room_PID).


