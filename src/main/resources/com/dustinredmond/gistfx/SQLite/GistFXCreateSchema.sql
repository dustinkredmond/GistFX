CREATE TABLE "GistFiles"
(
    "fileID"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "gistId"   TEXT,
    "filename" TEXT,
    "content"  TEXT,
    "dirty"    INT DEFAULT 0,
    CONSTRAINT "fk_GistFiles_Gists_1" FOREIGN KEY ("gistId") REFERENCES "Gists" ("gistId") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "Unique" UNIQUE ("gistId", "fileName") ON CONFLICT IGNORE
);

CREATE TABLE "GistFilesUndo"
(
    "fileID"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "historyID" INTEGER,
    "gistId"    TEXT,
    "filename"  TEXT,
    "content"   TEXT,
    "dirty"     INT DEFAULT 0,
    "timestamp" DATE,
    CONSTRAINT "fk_GistFilesUndo_GistFiles_1" FOREIGN KEY ("fileID") REFERENCES "GistFiles" ("fileID") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "Unique" UNIQUE ("gistId", "fileName") ON CONFLICT IGNORE
);

CREATE TABLE "Gists"
(
    "gistId"      TEXT NOT NULL ON CONFLICT ABORT,
    "name"        TEXT,
    "description" TEXT,
    "isPublic"    INT,
    "url"         TEXT,
    PRIMARY KEY ("gistId"),
    CONSTRAINT "gistId" UNIQUE ("gistId") ON CONFLICT IGNORE
);

CREATE TABLE "NameMap"
(
    "gistId" TEXT,
    "name"   TEXT,
    CONSTRAINT "fk_NameMap_Gists_1" FOREIGN KEY ("gistId") REFERENCES "Gists" ("gistId") ON DELETE CASCADE ON UPDATE CASCADE
);

