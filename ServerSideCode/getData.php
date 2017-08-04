<?php

// Suppress Warnings
error_reporting(E_ALL ^ E_WARNING);

// Error Codes
define("NO_SONG_FOUND"     ,     1);
define("NO_ALBUM_FOUND"    ,     2);
define("NO_ARTIST_FOUND"   ,     3);
define("MISSING_ARTIST"    ,     4);
define("INV_ACT_CODE"      ,     5);
define("MISSING_ACT_CODE"  ,     6);
define("MISSING_SFL"       ,     7);
define("DB_QUERY_ERROR"    ,     7);

// Json Codes
define("JSON_SUCCESS","success");
define("JSON_ALBUM_ID","aid");
define("JSON_ALBUM_NAME","aln");
define("JSON_ALBUM_YEAR","aly");
define("JSON_SONG_ID","sid");
define("JSON_SONG_NAME","son");
define("JSON_SONG_LYRICS","sol");
define("JSON_SONG_FETCH_LIST","sfl");
define("JSON_ERROR_CODE","erc");
define("JSON_ALBUMS","albums");
define("JSON_SONGS","songs");
define("JSON_ACTION_CODE","ac");
define("JSON_ACTION_LIST","li");
define("JSON_ACTION_SEE","si");
define("JSON_ARTIST_NAME","an");


// Array for json response, assume success.
$response = array();
$response[JSON_SUCCESS]=1;

// include db connect class
require_once __DIR__ . '/db_config.php';
$mysqli = new mysqli(DB_SERVER, DB_USER, DB_PASSWORD, DB_DATABASE);

if (isset($_GET[JSON_ACTION_CODE])) {

	// User has provided action Code
    $action_code = $_GET[JSON_ACTION_CODE];
    if($action_code==JSON_ACTION_LIST){
    	$isSuccess = true;
    	// User requested Album/Song List for Artist
    	if(isset($_GET[JSON_ARTIST_NAME])){
    		$artist_name = $_GET[JSON_ARTIST_NAME];
    		$result1 = $mysqli->query("SELECT * FROM ARTISTS WHERE ARTIST_NAME='$artist_name'");
			if ($result1) {
				if ($result1->num_rows > 0) {
					// Artist found
					$row = $result1->fetch_assoc();
					$artist_id=$row["ARTIST_ID"];
					$result2 = $mysqli->query("Select ALBUM_ID FROM ALBUMS WHERE ARTIST_ID=$artist_id ORDER BY ALBUM_ID DESC");
					if($result2){
						if($result2->num_rows > 0){	
							// Albums found
							$response[JSON_ALBUMS] = array();
							while($row=$result2->fetch_assoc()){
								$album_array = array();
								$album_id = $row["ALBUM_ID"];
								$album_array[JSON_ALBUM_ID]=$album_id;
								$result3 = $mysqli->query("Select SONG_ID FROM SONGS WHERE ALBUM_ID='$album_id' ORDER BY SONG_ID");
								if ($result3) {
									if ($result3->num_rows > 0) {
										// Songs found
										$album_array[JSON_SONG_ID] = array();
										while($row=$result3->fetch_assoc()){
											// Push Song details.
											array_push($album_array[JSON_SONG_ID],$row["SONG_ID"]);
										}
									}else{ $isSuccess = false; sendError(NO_SONG_FOUND); break; }
								}else{ $isSuccess = false; sendError(DB_QUERY_ERROR); break;}
								// Push Album details.
								array_push($response[JSON_ALBUMS], $album_array);
							}
						} else{ $isSuccess = false; sendError(NO_ALBUM_FOUND); }
					} else{ $isSuccess = false; sendError(DB_QUERY_ERROR); }
				} else{ $isSuccess = false; sendError(NO_ARTIST_FOUND); }
    		} else { $isSuccess = false; sendError(DB_QUERY_ERROR); }
    	} else { $isSuccess = false; sendError(MISSING_ARTIST); }
    	
    	if($isSuccess){
    		echo json_encode($response);
    	}
    } else if ($action_code==JSON_ACTION_SEE){
    	// Send Lyrics.

    	$isSuccess = true;
    	if(isset($_GET[JSON_SONG_FETCH_LIST])){
    		
    		$response[JSON_ALBUMS] = array();
    		
    		$string_album_song = $_GET[JSON_SONG_FETCH_LIST];
    		$all_album_array = explode("+", $string_album_song);
    		for($i=0;$i<count($all_album_array);$i++){
    			// Iterate over albums.
    			$server_album_array = array();
    			$album_song_array = explode(":", $all_album_array[$i]);
    			$album_id = $album_song_array[0];
    			$all_song_string =$album_song_array[1];
    			$songs_array = explode(",", $all_song_string);
    			$result1 = $mysqli->query("SELECT ALBUM_NAME, ALBUM_YEAR FROM ALBUMS WHERE ALBUM_ID='$album_id'");
    			if ($result1) {
    				$row = $result1->fetch_assoc();
    				$server_album_array[JSON_ALBUM_ID] = $album_id;
    				$server_album_array[JSON_ALBUM_NAME] = $row["ALBUM_NAME"];
    				$server_album_array[JSON_ALBUM_YEAR] = $row["ALBUM_YEAR"];
    				$server_album_array[JSON_SONGS] = array();
	    			for($j=0;$j<count($songs_array);$j++){
	    				// Iterate over list
	    				$song_id = $songs_array[$j];
	    				$result2 = $mysqli->query("SELECT SONG_NAME, SONG_LYRICS FROM SONGS WHERE SONG_ID='$song_id'");
	    				if($result2){
	    					$row = $result2->fetch_assoc();
	    					$temp_song_array = array();
	    					$temp_song_array[JSON_SONG_ID] = $song_id;
	    					$temp_song_array[JSON_SONG_NAME] = $row["SONG_NAME"];
	    					$temp_song_array[JSON_SONG_LYRICS] = $row["SONG_LYRICS"];
	    					array_push($server_album_array[JSON_SONGS], $temp_song_array);
	    				}else { $isSuccess = false; sendError(DB_QUERY_ERROR); break; }
	    			}
	    			array_push($response[JSON_ALBUMS],$server_album_array);
    			}else { $isSuccess = false; sendError(DB_QUERY_ERROR); break; }
    		}
    		if($isSuccess){
    			echo json_encode($response);
    		}
    	}else {sendError(MISSING_SFL); }
    }
    else{ sendError(INV_ACT_CODE); }
    
} else {
	sendError(MISSING_ACT_CODE);
}
mysqli_close($mysqli);

function sendError($error_code){
	$error_response = array();
	$error_response[JSON_SUCCESS]=0;
	$error_response[JSON_ERROR_CODE]=$error_code;
	echo json_encode($error_response);
}
?>