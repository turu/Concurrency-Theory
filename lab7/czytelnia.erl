-module(czytelnia).
-export( [ writer/1, reader/1, buffer/3, start/0, create_readers/2 ] ).

buffer(Readers_amount, Writer_Locked, Writer_PID) ->
    receive 
        {start_writing, Writer_Sender} ->
            if 
                (Writer_Locked == 0) and (Readers_amount == 0) ->
                    Writer_Sender ! can_write,
                    buffer( Readers_amount, 1, Writer_Sender);
                (Writer_Locked == 0) ->
                    buffer( Readers_amount, 1, Writer_Sender);
                true ->
                    self() ! {start_writing, Writer_Sender},
                    buffer( Readers_amount, 1, Writer_PID)
            end;

        {stop_writing} ->
            buffer( Readers_amount, 0, -1 );

        {start_reading, Reader_Sender} ->
            if
                Writer_Locked == 0 ->
                    Reader_Sender ! can_read,
                    buffer( Readers_amount + 1, 0, -1 );
                true ->
                    self() ! {start_reading, Reader_Sender},
                    buffer( Readers_amount, Writer_Locked, Writer_PID )
            end;
            
        {stop_reading} ->
            if 
                (Readers_amount == 1) and (Writer_Locked == 1) ->
                    Writer_PID ! can_write,
                    buffer( Readers_amount - 1, Writer_Locked, Writer_PID );
                true ->
                    buffer( Readers_amount - 1, Writer_Locked, Writer_PID )
            end
    end.

writer(Pid) ->
    timer:sleep( random:uniform(1000) ),
    Pid ! {start_writing, self()},
    receive
        can_write -> 
            io:fwrite( "writer came ~p\n", [ self() ] )
    end,
    timer:sleep( random:uniform(1000) ),
    Pid ! {stop_writing},
    io:fwrite( "writer finished ~p\n", [ self() ] ).

reader(Pid) ->
    timer:sleep( random:uniform(1000) ),
    Pid ! {start_reading, self()},
    receive
        can_read -> 
            io:fwrite( "reading started ~p\n", [ self() ] )
    end,
    timer:sleep( random:uniform(1000) ),
    Pid ! {stop_reading},
    io:fwrite( "reader is finished ~p\n", [ self() ] ).

start() ->
    Buffer_PID = spawn( czytelnia, buffer, [0, 0, -1] ),
    create_readers(5, Buffer_PID),
    create_writers(3, Buffer_PID).

create_readers(N, Buffer_PID) ->
    spawn( czytelnia, reader,[ Buffer_PID ] ),
    if
        N == 0 ->
            ok;
        true ->
            create_readers(N-1, Buffer_PID)
    end.

create_writers(N, Buffer_PID) ->
    spawn( czytelnia, writer, [ Buffer_PID ] ),
    if 
        N == 0 ->
            ok;
        true ->
            create_writers(N-1, Buffer_PID)
    end.

