package com.naldana.ejemplo10


import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.BaseColumns
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.RecyclerView.LayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.naldana.ejemplo10.Adapter.coinAdapter
import com.naldana.ejemplo10.Network.NetworkUtils
import com.naldana.ejemplo10.data.Database
import com.naldana.ejemplo10.data.DatabaseContract
import com.naldana.ejemplo10.model.infoAllCoin
import com.naldana.ejemplo10.model.infoCoins
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var dbHelper = Database(this) // TODO (12) Se crea una instancia del SQLiteHelper definido en la clase Database.
    var twoPane =  false
    private var coinList: ArrayList<infoCoins> = ArrayList()


    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Add coin", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        btn_refresh.setOnClickListener {
            FetchCoins().execute()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        // TODO (12) Con el Listener Creado se asigna al  DrawerLayout
        drawer_layout.addDrawerListener(toggle)


        // TODO(13) Se sincroniza el estado del menu con el LISTENER
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        // TODO (20) Para saber si estamos en modo dos paneles
        if (fragment_content != null ){
            twoPane =  true
        }

        initRecycler(lista)
        FetchCoins().execute()
        /*if(readCoins()==null){
            FetchCoins().execute()
        }
        else{
            readCoins()
            addCoinToList(infoCoins())
        }*/

    }

    private fun readCoins(): List<infoCoins> {

// TODO(13) Para obtener los datos almacenados, es necesario solicitar una instancia de lectura de la base de datos.
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseContract.CoinEntry.COLUMN_NAME,
            DatabaseContract.CoinEntry.COLUMN_COUNTRY,
            DatabaseContract.CoinEntry.COLUMN_VALUE,
            DatabaseContract.CoinEntry.COLUMN_VALUE_US,
            DatabaseContract.CoinEntry.COLUMN_YEAR,
            DatabaseContract.CoinEntry.COLUMN_REVIEW,
            DatabaseContract.CoinEntry.COLUMN_ISAVAILABLE,
            DatabaseContract.CoinEntry.COLUMN_IMG
        )

        val sortOrder = "${DatabaseContract.CoinEntry.COLUMN_NAME} DESC"

        val cursor = db.query(
            DatabaseContract.CoinEntry.TABLE_NAME, // nombre de la tabla
            projection, // columnas que se devolverán
            null, // Columns where clausule
            null, // values Where clausule
            null, // Do not group rows
            null, // do not filter by row
            sortOrder // sort order
        )

        var lista = mutableListOf<infoCoins>()

        with(cursor) {
            while (moveToNext()) {
                var coin = infoCoins(
                    getString(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_NAME)),
                    getString(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_COUNTRY)),
                    getDouble(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_VALUE)),
                    getDouble(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_VALUE_US)),
                    getInt(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_YEAR)),
                    getString(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_REVIEW)),
                    getInt(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_ISAVAILABLE)),
                    getString(getColumnIndexOrThrow(DatabaseContract.CoinEntry.COLUMN_IMG))
                )

                lista.add(coin)
            }
        }

        return lista
    }

    private fun coinItemClicked(item: infoCoins) {
        val coinBundle = Bundle()
        coinBundle.putParcelable("COIN", item)
        startActivity(Intent(this, DetailCoin::class.java).putExtras(coinBundle))
    }

    private lateinit var viewAdapter: coinAdapter
    private lateinit var viewManager: LayoutManager


    fun initRecycler(coins : ArrayList<infoCoins>) {

        //viewManager = LinearLayoutManager(this)
        if(this.resources.configuration.orientation == 2
            || this.resources.configuration.orientation == 4){
            viewManager = LinearLayoutManager(this)
        } else{
            viewManager = GridLayoutManager(this, 2)
        }

        viewAdapter = coinAdapter(coins, { coinItem: infoCoins -> coinItemClicked(coinItem) })

        recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

    fun addCoinToList(coins: infoCoins) {
        coinList.add(coins)
        viewAdapter.setMonedas(coinList)
        Log.d("Number", coinList.size.toString())
    }

    private var lista : ArrayList<infoCoins> = ArrayList<infoCoins>()

    private inner class FetchCoins() : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var url : URL = NetworkUtils.buiURL()
            try {
                var result : String = NetworkUtils.getResponseFromHttpUrl(url)
                return result
            } catch (e : IOException){
                e.printStackTrace()
                return ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var gson : Gson = Gson()
            var element : infoAllCoin = gson.fromJson(result, infoAllCoin::class.java)
            for (i in 0 .. (element.coins.size-1)){
                var dato : infoCoins = infoCoins(element.coins.get(i).name, element.coins.get(i).country,
                    element.coins.get(i).value,
                    element.coins.get(i).value_us,
                    element.coins.get(i).year,
                    element.coins.get(i).review,
                    element.coins.get(i).isAvailable,
                    element.coins.get(i).img)

                lista.add(dato)

                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(DatabaseContract.CoinEntry.COLUMN_NAME, dato.name)
                    put(DatabaseContract.CoinEntry.COLUMN_COUNTRY, dato.country)
                    put(DatabaseContract.CoinEntry.COLUMN_VALUE, dato.value)
                    put(DatabaseContract.CoinEntry.COLUMN_VALUE_US, dato.value_us)
                    put(DatabaseContract.CoinEntry.COLUMN_YEAR, dato.year)
                    put(DatabaseContract.CoinEntry.COLUMN_REVIEW, dato.review)
                    put(DatabaseContract.CoinEntry.COLUMN_ISAVAILABLE, dato.isAvailable)
                    put(DatabaseContract.CoinEntry.COLUMN_IMG, dato.img)
                }

                val newRowId = db?.insert(DatabaseContract.CoinEntry.TABLE_NAME, null, values)

                if (newRowId == -1L) {
                    Snackbar.make(drawer_layout, getString(R.string.alert_person_not_saved), Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    Snackbar.make(drawer_layout, getString(R.string.alert_person_saved_success) + newRowId, Snackbar.LENGTH_SHORT)
                        .show()
                    viewAdapter.setMonedas(readCoins())
                }
            }
            //initRecycler(lista)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun searchForCountry(country : String){
        var listaS : ArrayList<infoCoins> = ArrayList<infoCoins>()
        for (i in 0 .. (lista.size-1)){
            if(lista.get(i).country.equals(country)){
                listaS.add(lista.get(i))
            }
            initRecycler(listaS)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_all -> {
                initRecycler(lista)
            }
            R.id.nav_sv -> {
                searchForCountry("El Salvador")
            }
            R.id.nav_mx -> {
                searchForCountry("Mexico")
            }
            R.id.nav_usa -> {
                searchForCountry("USA")
            }
            R.id.nav_vn -> {
                searchForCountry("Venezuela")
            }
            R.id.nav_gt -> {
                searchForCountry("Guatemala")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
