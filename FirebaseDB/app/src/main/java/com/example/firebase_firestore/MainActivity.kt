package com.example.firebase_firestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import org.checkerframework.common.returnsreceiver.qual.This

class MainActivity : AppCompatActivity() {
    private lateinit var etAge: EditText
    private lateinit var etLastName: EditText
    private lateinit var etFirstname: EditText
    private lateinit var txtfrom: EditText
    private lateinit var txtto: EditText
    private lateinit var newAge: EditText
    private lateinit var newFirstName: EditText
    private lateinit var newLastName: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnRe: Button
    private lateinit var btnUpd: Button
    private lateinit var infoPerson: TextView
    private lateinit var btnDeletePer: Button
    private val personCollectionRef = Firebase.firestore.collection("persons")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Text Fields for data
        etFirstname = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etAge = findViewById(R.id.etAge)

        //Text Fields to update Data
        newAge = findViewById(R.id.etNewAge)
        newFirstName = findViewById(R.id.etNewFirstName)
        newLastName = findViewById(R.id.etNewLastName)

        //Filter By Age
        txtfrom = findViewById(R.id.etFrom)
        txtto = findViewById(R.id.etTo)

        //Buttons for actions
        btnUpload = findViewById(R.id.btnUploadData)
        btnUpd = findViewById(R.id.btnUpdatePerson)
        btnRe = findViewById(R.id.btnRetrieveData)
        btnDeletePer = findViewById(R.id.btnDeletePerson)

        //section to see info
        infoPerson = findViewById(R.id.tvPersons)

        btnUpload.setOnClickListener{
            val person = getOldPerson()
            savePerson(person)
        }

        //Real time Information
        subscribeToRealtimeUpdates()

        btnRe.setOnClickListener{
            retrievePerson()
        }

        btnUpd.setOnClickListener{
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()

            updatePerson(oldPerson, newPersonMap)
        }
        btnDeletePer.setOnClickListener {
            val oldPerson = getOldPerson()
            deletePerson(oldPerson)
        }

    }


    private fun getOldPerson(): Person{
        val firstname = etFirstname.text.toString()
        val lastname =  etLastName.text.toString()
        val age = etAge.text.toString().toInt()
        return Person(firstname, lastname, age)
    }

    private fun getNewPersonMap(): Map<String, Any>{
        val firstname = newFirstName.text.toString()
        val secondName = newLastName.text.toString()
        val age = newAge.text.toString()
        val map = mutableMapOf<String, Any>()
        if(firstname.isNotEmpty()){
            map["firstname"] = firstname

        }
        if(secondName.isNotEmpty()){
            map["secondName"] = secondName

        }
        if(age.isNotEmpty()){
            map["age"] = age.toInt()
        }
        return map

    }


    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstname", person.firstname)
            .whereEqualTo("secondName", person.secondName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    personCollectionRef.document(document.id).delete().await()
                    /*personCollectionRef.document(document.id).update(mapOf(
                        "firstName" to FieldValue.delete()
                    )).await()*/
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No persons matched the query.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch{
       val personQuery = personCollectionRef
            .whereEqualTo("firstname", person.firstname)
            .whereEqualTo("secondName", person.secondName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No persons matched the query.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrievePerson() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = txtfrom.text.toString().toInt()
        val toAge = txtto.text.toString().toInt()
        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                .get()
                .await()
            val sb = StringBuilder()
            for(document in querySnapshot.documents){
                val person = document.toObject<Person>()
                 sb.append("$person\n")
            }
            withContext(Dispatchers.Main){
                infoPerson.text = sb.toString()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun subscribeToRealtimeUpdates() {
        personCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb = StringBuilder()
                for(document in it) {
                    val person = document.toObject<Person>()
                    sb.append("$person\n")
                }
                infoPerson.text = sb.toString()
            }
        }
    }

    private fun savePerson(person: Person)= CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully Saved Data", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}