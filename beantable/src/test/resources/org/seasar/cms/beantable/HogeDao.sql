getDtos=\
    SELECT * FROM hoge

getDtoById=\
    SELECT * FROM hoge WHERE id=?

getDtoByIdAndUsername=\
    SELECT * FROM hoge WHERE id=? AND username=?

getDtosByUsername=\
    SELECT * FROM hoge WHERE username=:username

getDtoByUsername=\
    SELECT * FROM hoge WHERE username=:username

updateById=\
    id=?

delete=\
    DELETE FROM hoge

deleteById=\
    DELETE FROM hoge WHERE id=?

getDtoCount=\
    SELECT COUNT(*) FROM hoge

getIds=\
    SELECT id FROM hoge

getUsername=\
    SELECT username FROM hoge

getUsernames=\
    SELECT username FROM hoge

getUsername2=\
    SELECT username FROM hoge

getUsernames2=\
    SELECT username FROM hoge
