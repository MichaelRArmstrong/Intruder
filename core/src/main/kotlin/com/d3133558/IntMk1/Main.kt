package com.d3133558.IntMk1

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.d3133558.IntMk1.GameScreen

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : Game() {
    override fun create() {
        setScreen(MenuScreen(this))
    }
}
