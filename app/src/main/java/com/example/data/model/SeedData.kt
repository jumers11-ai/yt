package com.example.data.model

object SeedData {
    val initialTracks = listOf(
        CachedTrack(
            id = "spotify_1",
            title = "Midnight Horizon",
            artist = "Lune & Echo",
            album = "Stargazing Nights",
            durationMs = 234000L,
            artworkUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=400",
            provider = "Spotify",
            lyrics = """
                [00:00] (Instrumental Intro)
                [00:15] Fading lights along the dark highway
                [00:28] We drive until the sun goes down to sleep
                [00:41] Meet me where the stars begin to pray
                [00:54] Underneath the promises we keep
                [01:07] Turn the radio up, feel the neon fire
                [01:20] Whispering of dreams we used to have
                [01:33] Climb the sky, we're reaching even higher
                [01:46] Across the midnight horizon we glide
                [01:59] (Guitar solo)
                [02:15] Outro fading out...
            """.trimIndent(),
            isFavorite = true
        ),
        CachedTrack(
            id = "youtube_1",
            title = "Hyperdrive",
            artist = "RetroSynth Project",
            album = "Neon Runway",
            durationMs = 182000L,
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=400",
            provider = "YouTube",
            lyrics = """
                [00:00] (Synth Arpeggio Start)
                [00:10] Laser rays shining in the night
                [00:22] Speeding past the echoes of the street
                [00:35] Shift gears into the absolute light
                [00:47] Feel the pulse beneath your racing feet
                [01:00] We are riding the hyperdrive!
                [01:12] No turning back, we are so alive!
                [01:25] (Keyboard rhythm solo)
                [01:45] Fast stream ahead...
            """.trimIndent()
        ),
        CachedTrack(
            id = "soundcloud_1",
            title = "Acoustic Rain",
            artist = "Clara Wood",
            album = "Woodsmoke Sessions",
            durationMs = 210000L,
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&q=80&w=400",
            provider = "SoundCloud",
            lyrics = """
                [00:00] (Acoustic Guitar Intro)
                [00:12] Droplets on my window pane
                [00:25] Washing all our hurts away
                [00:38] Simple strings inside the rain
                [00:51] Teaching me the words to say
                [01:04] Oh, acoustic rain, fall on me
                [01:18] Set my heavy spirit free
                [01:32] (Melancholy chords)
                [01:50] Gently ending in the breeze...
            """.trimIndent(),
            isFavorite = true
        ),
        CachedTrack(
            id = "deezer_1",
            title = "Golden Hour Echoes",
            artist = "Sola Duo",
            album = "Equinox",
            durationMs = 285000L,
            artworkUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?auto=format&fit=crop&q=80&w=400",
            provider = "Deezer",
            lyrics = "Traditional electronic deep house ambient track. No lyrics found.",
            isFavorite = false
        ),
        CachedTrack(
            id = "tidal_1",
            title = "After Hours Jazz",
            artist = "The Miles Quartet",
            album = "Blue Note Corner",
            durationMs = 312000L,
            artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&q=80&w=400",
            provider = "Tidal",
            lyrics = "Instrumental jazz standard. Perfect for relaxing and focusing.",
            isFavorite = false
        ),
        CachedTrack(
            id = "applemusic_1",
            title = "Ascending Heights",
            artist = "The Orchestral Society",
            album = "Modern Symphonies",
            durationMs = 345000L,
            artworkUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?auto=format&fit=crop&q=80&w=400",
            provider = "Apple Music",
            lyrics = "Classical orchestration. Violin lead and grand piano backing.",
            isFavorite = false
        ),
        CachedTrack(
            id = "spotify_2",
            title = "Digital Ocean",
            artist = "CyberWaves",
            album = "AquaNet 2099",
            durationMs = 205000L,
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&q=80&w=400",
            provider = "Spotify",
            lyrics = """
                [00:00] (Water bubbling and soft ambient chords)
                [00:15] Diving deep, deep down below
                [00:30] Where the virtual corals grow
                [00:45] Bytes of water, streams of light
                [01:00] Swimming in the endless digital night
                [01:15] (Synthesizer solo)
                [01:35] Dive, dive, digital ocean...
            """.trimIndent()
        ),
        CachedTrack(
            id = "youtube_2",
            title = "Epic Orchestral Anthem",
            artist = "Legendary Vanguard",
            album = "World of Legends",
            durationMs = 290000L,
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&q=80&w=400",
            provider = "YouTube",
            lyrics = """
                [00:00] (Heavy brass opening)
                [00:20] We stand at the precipice of time
                [00:40] Together we march, together we fight
                [01:00] Facing the shadows, we will shine!
                [01:20] (Choir vocals)
                [01:45] Forward into glory!
            """.trimIndent()
        ),
        CachedTrack(
            id = "soundcloud_2",
            title = "Chilled Caffeine",
            artist = "Lofi Beans",
            album = "Roast & Brew",
            durationMs = 154000L,
            artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&q=80&w=400",
            provider = "SoundCloud",
            lyrics = "Instrumental Lofi. Grab a warm cup of coffee and sit back.",
            isFavorite = true
        )
    )
}
